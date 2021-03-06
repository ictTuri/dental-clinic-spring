package com.clinic.dental.model.report.dto;

import java.util.List;

import lombok.Data;

@Data
public class TotalRezervationsReportDto {
	private int year;
	private String monthName;
	private Long total;
	private Long done;
	private Long clientCancelled;
	private Long doctocCancelled;
	private List<WeeklyReportDto> weeklyReport;
}
