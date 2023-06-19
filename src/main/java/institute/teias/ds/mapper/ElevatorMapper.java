package institute.teias.ds.mapper;

import institute.teias.ds.elevator.Elevator;

public class ElevatorMapper {
    private Elevator elevator;
    public ElevatorMapper(Elevator elevator){
        this.elevator = elevator;
    }

    public int moveUp(){
        this.elevator.moveUp();
        return this.elevator.currentFloor();
    }

    public int moveDown(){
        this.elevator.moveDown();
        return this.elevator.currentFloor();
    }

    public int getCurrentFloor() {
        return this.elevator.currentFloor();
    }
}
