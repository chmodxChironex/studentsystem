package me.chironex.studentsystem.data;

import java.sql.Connection;

public interface ReconnectStrategy {

    Connection openConnection() throws Exception;
}
