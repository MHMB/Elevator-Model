package institute.teias.ds.elevator.interfaces;

import institute.teias.ds.elevator.enums.ElevatorStatus;
import institute.teias.ds.elevator.enums.ElevatorDirection;

public interface ElevatorFactory {
    public void moveUp();
    public void moveDown();
    public void addNewDestinatoin(Integer destination);
    public ElevatorDirection direction();
    public ElevatorStatus status();

}
