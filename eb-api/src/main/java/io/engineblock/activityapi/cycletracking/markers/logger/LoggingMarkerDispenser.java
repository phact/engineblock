/*
 *
 *    Copyright 2016 jshook
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 * /
 */

package io.engineblock.activityapi.cycletracking.markers.logger;

import com.google.auto.service.AutoService;
import io.engineblock.activityapi.Activity;
import io.engineblock.activityapi.cycletracking.markers.Marker;
import io.engineblock.activityapi.cycletracking.markers.MarkerDispenser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@AutoService(MarkerDispenser.class)
public class LoggingMarkerDispenser implements MarkerDispenser {

    private final static Logger logger = LoggerFactory.getLogger(LoggingMarkerDispenser.class);
    private Activity activity;

    @Override
    public String getName() {
        return "loggingmarker";
    }

    @Override
    public void setActivity(Activity activity) {
        this.activity = activity;
    }

    @Override
    public Marker getMarker(long slot) {
        return new LoggingMarker(activity.getActivityDef(), slot);
    }

}
