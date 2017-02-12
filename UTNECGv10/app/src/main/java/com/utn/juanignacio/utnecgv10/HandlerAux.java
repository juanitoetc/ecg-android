package com.utn.juanignacio.utnecgv10;

import android.os.Handler;

public class HandlerAux{

    public static final int	LETRA_H = 72;

    private static Handler handleraux;

    public static synchronized Handler getHandleraux() { return handleraux; }
    public static synchronized void setHandleraux(Handler handleraux){
        HandlerAux.handleraux = handleraux;
    }

}
