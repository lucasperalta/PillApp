package pillapp.Model;

import java.util.List;

/**
 * Created by lucas on 10/22/17.
 */

public class Paciente {



        String id;
        String paciente;
        String medico;

        List<Alarmas> alarmas;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPaciente() {
        return paciente;
    }

    public void setPaciente(String paciente) {
        this.paciente = paciente;
    }

    public String getMedico() {
        return medico;
    }

    public void setMedico(String medico) {
        this.medico = medico;
    }

    public List<Alarmas> getAlarmas() {
        return alarmas;
    }

    public void setAlarmas(List<Alarmas> alarmas) {
        this.alarmas = alarmas;
    }
}
