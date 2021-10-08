package com.clinic.dental.controllers;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.clinic.dental.model.appointment.converter.AppointmentConverter;
import com.clinic.dental.model.appointment.dto.ChangeTimeAppointmentDto;
import com.clinic.dental.model.appointment.dto.DisplayAppointmentDto;
import com.clinic.dental.model.appointment.dto.RezerveSlotDto;
import com.clinic.dental.model.appointment.dto.SlotDto;
import com.clinic.dental.model.appointment.service.AppointmentService;
import com.clinic.dental.util.AppointmentUtilTest;
import com.clinic.dental.util.ChangeTimeAppointmentDtoUtilTest;
import com.clinic.dental.util.RezerveSlotDtoUtilTest;

@ExtendWith(MockitoExtension.class)
class AppointmentControllerTest {

	@InjectMocks
	AppointmentController appointmentController;

	@Mock
	AppointmentService appointmentService;

	@BeforeEach
	void setup() {
		appointmentController = new AppointmentController(appointmentService);
	}

	@Test
	void givenFreeSchedule_WhenGetList_CheckValidateList() {
		List<SlotDto> slots = AppointmentUtilTest.getSLots();

		Mockito.when(appointmentService.getFreeTimes()).thenReturn(slots);

		ResponseEntity<List<SlotDto>> allSlots = appointmentController.getFreeTimes();

		assertEquals(HttpStatus.OK, allSlots.getStatusCode());
		assertNotNull(allSlots);
		assertEquals(true, allSlots.hasBody());
		verify(appointmentService).getFreeTimes();
	}

	@Test
	void givenAppointmentList_WhenGetList_CheckValidateSize() {
		ResponseEntity<List<DisplayAppointmentDto>> allAppointment = appointmentController.getMyAllAppointments();

		assertEquals(HttpStatus.OK, allAppointment.getStatusCode());
		assertNotNull(allAppointment);
		assertEquals(true, allAppointment.hasBody());
	}

	@Test
	void givenAppointment_WhenCancelled_VerifyStatus() {
		Long id = 1L;
		DisplayAppointmentDto dtoCancelled = AppointmentConverter.toDto(AppointmentUtilTest.appointmentFour());

		Mockito.when(appointmentService.cancelAppointment(id)).thenReturn(dtoCancelled);

		ResponseEntity<DisplayAppointmentDto> cancelledApp = appointmentController.cancelAppointment(id);

		assertEquals(HttpStatus.OK, cancelledApp.getStatusCode());
		assertNotNull(cancelledApp);
		assertEquals("USER_CANCELLED", cancelledApp.getBody().getStatus());
	}
	
	@Test
	void givenAppointment_WhenRefuzeNewTime_VerifyStatus() {
		DisplayAppointmentDto dtoRefuzed = AppointmentConverter.toDto(AppointmentUtilTest.appointmentUserRefuzed());

		Mockito.when(appointmentService.refuzeNewTimeAppointment(dtoRefuzed.getId())).thenReturn(dtoRefuzed);

		ResponseEntity<DisplayAppointmentDto> refuzedApp = appointmentController.refuzeNewTimeAppointment(dtoRefuzed.getId());

		assertEquals(HttpStatus.OK, refuzedApp.getStatusCode());
		assertNotNull(refuzedApp);
		assertEquals("USER_REFUZED_TIME", refuzedApp.getBody().getStatus());
	}

	@Test
	void givenAppointment_WhenApproveNewTime_VerifyStatus() {
		DisplayAppointmentDto dtoApproved = AppointmentConverter.toDto(AppointmentUtilTest.appointmentUserApprove());

		Mockito.when(appointmentService.approveNewTimeAppointment(dtoApproved.getId())).thenReturn(dtoApproved);

		ResponseEntity<DisplayAppointmentDto> approvedApp = appointmentController.approveNewTimeAppointment(dtoApproved.getId());

		assertEquals(HttpStatus.OK, approvedApp.getStatusCode());
		assertNotNull(approvedApp);
		assertEquals("ACTIVE", approvedApp.getBody().getStatus());
	}
	
	@Test
	void givenAppointment_WhenClose_VerifyStatus() {
		DisplayAppointmentDto dtoClosed = AppointmentConverter.toDto(AppointmentUtilTest.appointmentClosed());

		Mockito.when(appointmentService.closeAppointmentById(dtoClosed.getId())).thenReturn(dtoClosed);

		ResponseEntity<DisplayAppointmentDto> closedApp = appointmentController.closeAppointmentById(dtoClosed.getId());

		assertEquals(HttpStatus.OK, closedApp.getStatusCode());
		assertNotNull(closedApp);
		assertEquals("DONE", closedApp.getBody().getStatus());
	}
	
	@Test
	void givenAppointment_WhenApproveById_VerifyStatus() {
		DisplayAppointmentDto dtoApproved = AppointmentConverter.toDto(AppointmentUtilTest.appointmentUserApprove());

		Mockito.when(appointmentService.approveAppointmentById(dtoApproved.getId())).thenReturn(dtoApproved);

		ResponseEntity<DisplayAppointmentDto> approvedByIdApp = appointmentController.approveAppointmentById(dtoApproved.getId());

		assertEquals(HttpStatus.OK, approvedByIdApp.getStatusCode());
		assertNotNull(approvedByIdApp);
		assertEquals("ACTIVE", approvedByIdApp.getBody().getStatus());
	}
	
	@Test
	void givenAppointment_WhenGetById_VerifyAppointment() {
		DisplayAppointmentDto dtoGet = AppointmentConverter.toDto(AppointmentUtilTest.appointmentFour());

		Mockito.when(appointmentService.getAppointmentById(dtoGet.getId())).thenReturn(dtoGet);

		ResponseEntity<DisplayAppointmentDto> getByIdApp = appointmentController.getAppointmentById(dtoGet.getId());

		assertEquals(HttpStatus.OK, getByIdApp.getStatusCode());
		assertEquals("dentistname", getByIdApp.getBody().getDentist());
		assertEquals(LocalDate.of(2021, 11, 5), getByIdApp.getBody().getDate());
		assertNotNull(getByIdApp);
	}
	
	@Test
	void givenSlot_WhenRezerve_VerifyAppointment() {
		RezerveSlotDto dto = RezerveSlotDtoUtilTest.rezerveSlot();
		DisplayAppointmentDto dtoGet = AppointmentConverter.toDto(AppointmentUtilTest.rezervedAppointment());
		
		Mockito.when(appointmentService.rezerveAppointment(dto)).thenReturn(dtoGet);
		
		ResponseEntity<DisplayAppointmentDto> rezerveAppointmen = appointmentController.rezerveAppointment(dto);

		assertEquals(HttpStatus.CREATED, rezerveAppointmen.getStatusCode());
		assertEquals("dentistname", rezerveAppointmen.getBody().getDentist());
		assertEquals(LocalDate.of(2021, 11, 2), rezerveAppointmen.getBody().getDate());
		assertNotNull(rezerveAppointmen);
	}
	
	@Test
	void givenAppointment_WhenChangeTime_VerifyAppointment() {
		Long id = 1L;
		ChangeTimeAppointmentDto dtoNewTime = ChangeTimeAppointmentDtoUtilTest.changeTimeOne();
		DisplayAppointmentDto dtoChangeTime = AppointmentConverter.toDto(AppointmentUtilTest.appointmentFive());

		Mockito.when(appointmentService.changeAppointmentTimeById(id,dtoNewTime)).thenReturn(dtoChangeTime);

		ResponseEntity<DisplayAppointmentDto> cangeTimeApp = appointmentController.changeAppointmentTimeById(id,dtoNewTime);

		assertEquals(HttpStatus.OK, cangeTimeApp.getStatusCode());
		assertEquals("dentistname", cangeTimeApp.getBody().getDentist());
		assertEquals(LocalDate.of(2021, 11, 12), cangeTimeApp.getBody().getDate());
		assertNotNull(cangeTimeApp);
	}
	
}