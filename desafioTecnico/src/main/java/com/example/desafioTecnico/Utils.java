package com.example.desafioTecnico;

import java.util.Calendar;
import java.util.Date;

public class Utils {

    public static Date adicionarSegundos(Date data, int segundos) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(data);
        calendar.add(Calendar.SECOND, segundos);
        return calendar.getTime();
    }
}
