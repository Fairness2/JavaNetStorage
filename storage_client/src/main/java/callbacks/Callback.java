package callbacks;

import models.TransmittedSignal;

public interface Callback {
    void processAnswer(TransmittedSignal message);
}
