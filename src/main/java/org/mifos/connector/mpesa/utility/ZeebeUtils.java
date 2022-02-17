package org.mifos.connector.mpesa.utility;

import com.google.api.client.util.ExponentialBackOff;
import org.json.JSONObject;

import java.io.IOException;

public class ZeebeUtils {

    public static JSONObject getTransferResponseCreateJson() {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("completedTimestamp", ""+System.currentTimeMillis());
        return jsonObject;
    }

    /** takes initial timer in the ISO 8601 durations format
     * for more info check
     * https://docs.camunda.io/docs/0.26/reference/bpmn-workflows/timer-events/#time-duration
     *
     * @param initialTimer initial timer in the ISO 8601 durations format, ex: PT45S
     * @return next timer value in the ISO 8601 durations format
     */
    public static String getNextTimer(String initialTimer){
        String stringSecondsValue = initialTimer.split("T")[1].split("S")[0];
        int initialSeconds = Integer.parseInt(stringSecondsValue);
        int initSecInMs = initialSeconds * 1000;

        ExponentialBackOff backoff =
                new ExponentialBackOff.Builder()
                        .setInitialIntervalMillis(initSecInMs) // initial time
                        .setMaxIntervalMillis(initSecInMs+1)
                        .build();

        int next = 0;
        try {
            next = (int) backoff.nextBackOffMillis()/1000;
        } catch (IOException e) {
            return initialTimer;
        }

        return String.format("PT%sS", next);
    }

}
