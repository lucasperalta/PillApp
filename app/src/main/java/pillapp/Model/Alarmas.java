package pillapp.Model;

/**
 * Created by lucas on 10/22/17.
 */

public class Alarmas  {

    String nombre;
    String diaDeSemana;
    String hora;
    String minutos;

    public String getNombre() {
        return nombre;
    }

    public String getDiaDeSemana() {
        return diaDeSemana;
    }

    public void setDiaDeSemana(String diaDeSemana) {
        this.diaDeSemana = diaDeSemana;
    }

    public String getHora() {
        return hora;
    }

    public void setHora(String hora) {
        this.hora = hora;
    }

    public String getMinutos() {
        return minutos;
    }

    public void setMinutos(String minutos) {
        this.minutos = minutos;
    }

    public void setNombre(String nombre) {

        this.nombre = nombre;
    }
}
