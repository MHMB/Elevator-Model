package institute.teias.ds.elevator.interfaces;

public interface ElevatorControlSystemFactory {
        public void pickUp(Integer pickUpFloor);
        public void destination(Integer elevatorId, Integer destinationFloor);
        public void step();

}
