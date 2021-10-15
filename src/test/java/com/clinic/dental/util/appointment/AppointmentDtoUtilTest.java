package com.clinic.dental.util.appointment;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import com.clinic.dental.model.appointment.dto.ChangeAppointmentDentistDto;
import com.clinic.dental.model.appointment.dto.ChangeAppointmentTimeDto;
import com.clinic.dental.model.appointment.dto.CreatePublicAppointmentDto;
import com.clinic.dental.model.appointment.enums.AppointmentType;
import com.clinic.dental.model.feedback.dto.CreateFeedbackDto;

public class AppointmentDtoUtilTest {
	public static ChangeAppointmentTimeDto changeTimeOne() {
		ChangeAppointmentTimeDto dto = new ChangeAppointmentTimeDto();
		dto.setDay(LocalDate.of(2021, 11, 12));
		dto.setStartTime(LocalTime.of(15, 00));
		return dto;
	}
	
	public static ChangeAppointmentDentistDto changeDentistOne() {
		ChangeAppointmentDentistDto dto = new ChangeAppointmentDentistDto();
		dto.setDentist("newDentist");
		return dto;
	}
	
	public static CreatePublicAppointmentDto rezerveSlot() {
		CreatePublicAppointmentDto dto = new CreatePublicAppointmentDto();
		dto.setDate(LocalDate.of(2021, 10, 14));
		dto.setDentist("doctor");
		dto.setStartTime(LocalTime.of(15, 00));
		dto.setType(AppointmentType.GENERAL.name());
		return dto;
	}
	
	public static String[] getDoctors(){
		List<String> list = new ArrayList<>();
		list.add("doctorname");
		list.add("doctorOne");
		String[] returnList = list.toArray(new String[0]);
		return returnList;
	}

	public static CreateFeedbackDto feedbackOne() {
		CreateFeedbackDto feedback = new CreateFeedbackDto();
		feedback.setDescription("Just For testing purposes!");
		return feedback;
	}
}