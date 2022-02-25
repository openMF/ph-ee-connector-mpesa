package org.mifos.connector.mpesa.utility;


import com.google.api.client.util.ExponentialBackOff;
import org.apache.camel.util.json.JsonObject;


import java.io.IOException;

public class ZeebeUtils {

    public static JsonObject getTransferResponseCreateJson() {
        JsonObject jsonObject = new JsonObject();
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

        int currentPower = (int) ( Math.log(initialSeconds) / Math.log(2) );
        int next = (int) Math.pow(2, ++currentPower);

        return String.format("PT%sS", next);
    }

}
