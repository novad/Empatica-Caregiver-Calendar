package nova.daniel.empatica.model;

import androidx.room.Embedded;
import androidx.room.Relation;

import java.util.List;

/**
 * Relation POJO class to represent all {@link Caregiver} instances with a list of associated {@link Appointment}.
 * If the caregiver does not have any associated Appointment, the list is empty.
 */
public class CaregiverWAppointments {
    @Embedded
    public Caregiver caregiver;

    @Relation(parentColumn = "uuid", entityColumn = "uuid")
    public List<Appointment> appointments;
}
