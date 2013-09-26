package playground.michalm.taxi.optimizer;

import java.util.*;

import pl.poznan.put.vrp.dynamic.data.VrpData;
import pl.poznan.put.vrp.dynamic.data.model.*;
import pl.poznan.put.vrp.dynamic.data.model.Request.ReqStatus;
import pl.poznan.put.vrp.dynamic.data.schedule.*;
import pl.poznan.put.vrp.dynamic.data.schedule.Schedule.ScheduleStatus;
import pl.poznan.put.vrp.dynamic.data.schedule.impl.WaitTaskImpl;


/**
 * The assumptions made:
 * <p>
 * a. all requests are queued according to the time of arrival (this implies the priority)
 * <p>
 * b. requests are scheduled one-by-one; while scheduling request A, other requests queued are not
 * taken into account
 * 
 * @author michalm
 */
public abstract class AbstractTaxiOptimizer
    implements TaxiOptimizer
{
    protected final VrpData data;
    protected Queue<Request> unplannedRequestQueue;


    public AbstractTaxiOptimizer(VrpData data)
    {
        this.data = data;
        int initialCapacity = data.getVehicles().size();// just proportional to the number of
                                                        // vehicles (for easy scaling)
        unplannedRequestQueue = new PriorityQueue<Request>(initialCapacity,
                new Comparator<Request>() {
                    public int compare(Request r1, Request r2)
                    {
                        return r1.getId() - r2.getId();
                    }
                });
    }


    /**
     */
    @Override
    public void init()
    {
        // Initial remark!!!
        // We start with 0 requests so there is no need for any pre-optimization.
        // Let's just add a WAIT task to each schedule
        for (Vehicle veh : data.getVehicles()) {
            veh.getSchedule().addTask(
                    new WaitTaskImpl(veh.getT0(), veh.getT1(), veh.getDepot().getVertex()));
        }
    }


    @Override
    public void taxiRequestSubmitted(Request request)
    {
        unplannedRequestQueue.add(request);

        optimize();
    }


    protected abstract boolean shouldOptimizeBeforeNextTask(Vehicle vehicle, boolean scheduleUpdated);


    protected abstract boolean shouldOptimizeAfterNextTask(Vehicle vehicle, boolean scheduleUpdated);


    @Override
    public void nextTask(Vehicle vehicle)
    {
        Schedule schedule = vehicle.getSchedule();

        boolean scheduleUpdated = false;

        // Assumption: there is no delay as long as the schedule has not been started (PLANNED)
        if (schedule.getStatus() == ScheduleStatus.STARTED) {
            scheduleUpdated = updateBeforeNextTask(vehicle);
        }

        if (shouldOptimizeBeforeNextTask(vehicle, scheduleUpdated)) {
            optimize();
        }

        schedule.nextTask();

        if (shouldOptimizeAfterNextTask(vehicle, scheduleUpdated)) {// after nextTask()
            optimize();
        }
    }


    /**
     * Check and decide if the schedule should be updated due to if vehicle is Update timings (i.e.
     * beginTime and endTime) of all tasks in the schedule.
     * 
     * @return <code>true</code> if there have been significant changes in the schedule hence the
     *         schedule needs to be re-optimized
     */
    protected abstract boolean updateBeforeNextTask(Vehicle vehicle);


    /**
     * Try to schedule all unplanned tasks (if any)
     */
    protected void optimize()
    {
        while (!unplannedRequestQueue.isEmpty()) {
            Request req = unplannedRequestQueue.peek();

            if (req.getStatus() != ReqStatus.UNPLANNED) {
                throw new IllegalStateException();
            }

            scheduleRequest(req);// means: try to schedule

            if (req.getStatus() == ReqStatus.UNPLANNED) {
                return;// no taxi available
            }
            else {
                unplannedRequestQueue.poll();
            }
        }
    }


    protected abstract void scheduleRequest(Request request);
}
