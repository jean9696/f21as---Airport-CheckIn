import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;

/**
 * Contains flights and bookings available in database
 * Has the main function that displays the GUI to checkIn passengers
 */
public class CheckIn {
    private static HashMap<String, Flight> flights;

    private static HashMap<String, Booking> bookings;

    private Integer checkInPassenger;

    /**
     * @param flights
     * @param bookings
     */
    public CheckIn(HashMap<String, Flight> flights, HashMap<String, Booking> bookings) {
        CheckIn.flights = flights;
        CheckIn.bookings = bookings;
        this.checkInPassenger = 0;
    }

    /**
     * @return all flights
     */
    public HashMap<String, Flight> getFlights() {
        return flights;
    }

    /**
     * @return all bookings
     */
    public HashMap<String, Booking> getBookings() {
        return bookings;
    }

    /**
     * @param key
     * @return a specific flight found by key
     */
    public Flight getFlight(String key) {
        return flights.get(key);
    }

    /**
     * @param key
     * @return a specific flight found by key
     */
    public Booking getBooking(String key) {
        return bookings.get(key);
    }

    /**
     * @return how many passenger has checked in
     */
    public Integer getCheckInPassenger() {
        return checkInPassenger;
    }

    /**
     * Check in the passenger by incrementing the total of passengers who have checkedIn
     * and set add this passenger baggage to the corresponding flight
     * @param booking
     * @param passengerBaggage
     */
    public void checkInPassenger(Booking booking, BaggageSize passengerBaggage) {
        booking.setCheckedIn(true);
        Flight passengerFlight = booking.getFlight();
        passengerFlight.addOnePassenger();
        passengerFlight.addBaggageRegistered(passengerBaggage);
        checkInPassenger++;
    }

    /**
     * Polymorphism of checkInPassenger above to increment extra fees counter
     * @param booking
     * @param passengerBaggage
     * @param extraFees
     */
    public void checkInPassenger (Booking booking, BaggageSize passengerBaggage, Integer extraFees) {
        Flight passengerFlight = booking.getFlight();
        passengerFlight.addExtraFees(extraFees);
        checkInPassenger(booking, passengerBaggage);
    }

    /**
     * @param args
     * Starting point of the program, initialize variables and launch the GUI with the listener
     */
    public static void main (String[] args) throws Exception {

        // should come from files
        final HashMap<String, Flight> flights = FileHelper.readFlightsFromInputFiles();
        HashMap<String, Booking> bookings = FileHelper.readBookingsFromInputFiles(flights);

        final Integer bookingNumber = bookings.size();
        final CheckIn checkIn = new CheckIn(flights, bookings);
        final GUI GUI = new GUI();
        ActionListener handleConfirm = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Booking booking = checkIn.getBooking(GUI.getBookingReferenceInput());
                if (booking != null && booking.canPassengerAccess(GUI.getSurnameInput(), GUI.getLastNameInput())) {
                    if (!booking.getCheckedIn()) {
                        BaggageSize passengerBaggage = new BaggageSize(GUI.getBaggageWeightInput(), GUI.getBaggageVolumeInput());
                        if (passengerBaggage.isValidSize()) {
                            // if passenger baggage are over capacity, the GUI shows a dialog and the user has to
                            // to be able to check in
                            if (passengerBaggage.isOverCapacity(booking.getBaggageSize())) {
                                Integer extraFees = passengerBaggage.calculateOverCapacityPrice(booking.getBaggageSize());
                                if (GUI.printOverCapacityConfirmDialog(extraFees)) {
                                    checkIn.checkInPassenger(booking, passengerBaggage, extraFees);
                                    GUI.clear();
                                }
                            } else {
                                checkIn.checkInPassenger(booking, passengerBaggage);
                                GUI.clear();
                            }
                            // when every passenger have checked in the program print the report
                            if (bookingNumber.equals(checkIn.getCheckInPassenger())) {
                                GUI.close();
                                FileHelper.writeToFile("Report.txt", FileHelper.makeReport(CheckIn.flights));
                                System.exit(0);
                            }
                        } else {
                            GUI.setMessage("Invalid baggage size inputs");
                        }
                    } else {
                        GUI.setMessage("This passenger has already checkedIn");
                    }
                } else {
                    GUI.setMessage("Invalid passenger inputs");
                }
            }
        };
        GUI.setOnConfirm(handleConfirm);
        
    }

}
