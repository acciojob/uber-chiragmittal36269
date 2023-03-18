package com.driver.services.impl;

import com.driver.model.TripBooking;
import com.driver.services.CustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.driver.model.Customer;
import com.driver.model.Driver;
import com.driver.repository.CustomerRepository;
import com.driver.repository.DriverRepository;
import com.driver.repository.TripBookingRepository;
import com.driver.model.TripStatus;

import java.util.List;

@Service
public class CustomerServiceImpl implements CustomerService {

	@Autowired
	CustomerRepository customerRepository2;

	@Autowired
	DriverRepository driverRepository2;

	@Autowired
	TripBookingRepository tripBookingRepository2;

	@Override
	public void register(Customer customer) {
		//Save the customer in database
		customerRepository2.save(customer);
	}

	@Override
	public void deleteCustomer(Integer customerId) {
		// Delete customer without using deleteById function

		Customer customer = customerRepository2.findById(customerId).get();

		customerRepository2.delete(customer);

		List<TripBooking> tripBookingList = customer.getTripBookingList();
		for(TripBooking t : tripBookingList)
		{
			t.setCustomer(null);
		}

	}

	@Override
	public TripBooking bookTrip(int customerId, String fromLocation, String toLocation, int distanceInKm) throws Exception {
		//Book the driver with lowest driverId who is free (cab available variable is Boolean.TRUE). If no driver is available, throw "No cab available!" exception
		//Avoid using SQL query

		try {
			Driver newDriver = new Driver();
			boolean marker = true;

			List<Driver> drivers = driverRepository2.findAll();
			for (Driver d : drivers) {
				if (d.getCab().getAvailable()) {
					newDriver = d;
					marker = false;
					break;
				}
			}
			if (marker) {
				throw new Exception("No cab available!");
			}

			// set the cab is not available
			newDriver.getCab().setAvailable(false);

			Customer customer = customerRepository2.findById(customerId).get();

			TripBooking tripBooking = new TripBooking();
			tripBooking.setFromLocation(fromLocation);
			tripBooking.setToLocation(toLocation);
			tripBooking.setDistanceInKm(distanceInKm);
			tripBooking.setStatus(TripStatus.CONFIRMED);
			tripBooking.setDriver(newDriver);

			int totalBill = distanceInKm * 10;
			tripBooking.setBill(totalBill);
			tripBooking.setCustomer(customer);

			// add trip in customer
			List<TripBooking> tripBookingList = customer.getTripBookingList();
			tripBookingList.add(tripBooking);

			// add trip in driver
			List<TripBooking> tripBookingList1 = newDriver.getTripBookingList();
			tripBookingList1.add(tripBooking);

			customerRepository2.save(customer);
			driverRepository2.save(newDriver);

			return tripBooking;

		} catch (Exception e) {
			throw new Exception(e.getMessage());
		}
	}

	@Override
	public void cancelTrip(Integer tripId){
		//Cancel the trip having given trip Id and update TripBooking attributes accordingly

		TripBooking tripBooking = tripBookingRepository2.findById(tripId).get();
		tripBooking.setStatus(TripStatus.CANCELED);
//		tripBooking.setToLocation(null);
//		tripBooking.setFromLocation(null);
		tripBooking.setBill(0);
		tripBooking.setDistanceInKm(0);



//		Customer customer = tripBooking.getCustomer();
//		List<TripBooking> tripBookingList = customer.getTripBookingList();
//		tripBookingList.remove(tripBooking);

		Driver driver = tripBooking.getDriver();
//		List<TripBooking> tripBookingList1 = driver.getTripBookingList();
//		tripBookingList1.remove(tripBooking);
		driver.getCab().setAvailable(true);

		driverRepository2.save(driver);

		tripBookingRepository2.save(tripBooking);
	}

	@Override
	public void completeTrip(Integer tripId){
		//Complete the trip having given trip Id and update TripBooking attributes accordingly

		TripBooking tripBooking = tripBookingRepository2.findById(tripId).get();
		tripBooking.setStatus(TripStatus.COMPLETED);

		Driver driver = tripBooking.getDriver();
		driver.getCab().setAvailable(true);

		driverRepository2.save(driver);

	}
}
