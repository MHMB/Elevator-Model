package institute.teias.ds.mapper;

import institute.teias.ds.elevator.Elevator;
import institute.teias.ds.elevator.ElevatorControlSystem;
import institute.teias.ds.elevator.enums.ElevatorDirection;
import institute.teias.ds.elevator.enums.ElevatorStatus;

public class ElevatorCSMapper {
    private ElevatorControlSystem elevatorControlSystem;
    public ElevatorCSMapper(ElevatorControlSystem elevatorControlSystem){
        this.elevatorControlSystem = elevatorControlSystem;
    }

    public int currentFloor(){
        Elevator elevator = elevatorControlSystem.getElevators().get(0);
        return  elevator.currentFloor();
    }

    public ElevatorStatus elevatorStatus(){
        Elevator elevator = elevatorControlSystem.getElevators().get(0);
        return elevator.status();
    }

    public ElevatorDirection stepTime(){
        elevatorControlSystem.step();
        Elevator elevator = elevatorControlSystem.getElevators().get(0);
        return elevator.direction();
    }

    public boolean pickUpPassenger(Integer pickUpFloor) {
        try {
            elevatorControlSystem.pickUp(pickUpFloor);
            return true;
        }
        catch (Exception e) {
            return false;
        }
    }
}
