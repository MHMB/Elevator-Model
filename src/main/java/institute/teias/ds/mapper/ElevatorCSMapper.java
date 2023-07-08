package institute.teias.ds.mapper;

import institute.teias.ds.elevator.Elevator;
import institute.teias.ds.elevator.ElevatorControlSystem;
import institute.teias.ds.elevator.enums.ElevatorDirection;
import institute.teias.ds.elevator.enums.ElevatorStatus;
import institute.teias.ds.elevator.exceptions.InvalidNumber;

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

    public ElevatorDirection pickUpPassenger(Integer pickUpFloor) throws InvalidNumber {
        try {
            elevatorControlSystem.pickUp(pickUpFloor);
            Elevator elevator = elevatorControlSystem.getElevators().get(0);
            return elevator.direction();
        }
        catch (Exception e) {
            throw new InvalidNumber("The provided number is invalid");
        }
    }
}
