document.addEventListener("DOMContentLoaded", function () {
  // Get CSRF token if using Spring Security
  const csrfToken = document.querySelector('meta[name="_csrf"]')?.content;
  const csrfHeader = document.querySelector('meta[name="_csrf_header"]')?.content;

  // Initialize calendar
  const calendarEl = document.getElementById("calendar");
  let selectedDateCell = null;

  const calendar = new FullCalendar.Calendar(calendarEl, {
    initialView: "dayGridMonth",
    height: 500,
    selectable: true,
    events: "/api/appointments/calendar",
    dateClick: function(info) {
      console.log("Date clicked:", info.dateStr);
      // Update the appointmentDate input with the clicked date in YYYY-MM-DD format
      const appointmentDateInput = document.getElementById("appointmentDate");
      appointmentDateInput.value = info.dateStr;
      // Manually trigger change event to update doctors list
      appointmentDateInput.dispatchEvent(new Event('change'));

      // Remove highlight from previously selected date cell
      if (selectedDateCell) {
        selectedDateCell.classList.remove("selected-date");
      }
      // Find the clicked date cell element by data-date attribute
      const newSelectedCell = calendarEl.querySelector(`[data-date="${info.dateStr}"]`);
      if (newSelectedCell) {
        newSelectedCell.classList.add("selected-date");
        selectedDateCell = newSelectedCell;
      }
    },
    // ... rest of your calendar config
  });
  calendar.render();

  // Current user - you might get this from a session or JWT
  function getCurrentPatientId() {
    // TODO: Replace with actual logic to get current patient ID
    return 1;
  }
  const currentPatientId = getCurrentPatientId();

  // Fetch doctors
  async function fetchDoctors(date) {
    try {
      const headers = {
        "Content-Type": "application/json",
      };
      if (csrfToken && csrfHeader) {
        headers[csrfHeader] = csrfToken;
      }

      let url = "/api/doctors";
      if (date) {
        url += `?date=${date}`;
      }

      const response = await fetch(url, {
        method: "GET",
        headers: headers,
      });

      if (!response.ok) throw new Error("Failed to fetch doctors");

      const doctors = await response.json();
      const doctorSelect = document.getElementById("doctorSelect");
      doctorSelect.innerHTML = '<option value="" disabled selected>Select a doctor</option>';

      doctors.forEach((doc) => {
        const option = document.createElement("option");
        option.value = doc.id;
        option.textContent = `${doc.firstName} ${doc.lastName}`;
        doctorSelect.appendChild(option);
      });
    } catch (error) {
      console.error("Error:", error);
      showError("Error loading doctors. Please try again.");
    }
  }

  // Listen for changes on appointmentDate input to fetch doctors for selected date
  const appointmentDateInput = document.getElementById("appointmentDate");
  if (appointmentDateInput) {
    appointmentDateInput.addEventListener("change", (e) => {
      const selectedDate = e.target.value;
      console.log("Date input changed:", selectedDate);
      if (selectedDate) {
        fetchDoctors(selectedDate);
        fetchAvailableSlots();
      } else {
        fetchDoctors();
        clearTimeSlots();
      }
    });
  }

  // Listen for changes on doctorSelect to fetch available slots
  const doctorSelectInput = document.getElementById("doctorSelect");
  if (doctorSelectInput) {
    doctorSelectInput.addEventListener("change", (e) => {
      fetchAvailableSlots();
    });
  }

  async function fetchAvailableSlots() {
    const doctorSelect = document.getElementById("doctorSelect");
    const appointmentDate = document.getElementById("appointmentDate").value;

    if (!doctorSelect.value || !appointmentDate) {
      clearTimeSlots();
      return;
    }

    try {
      const response = await fetch(`/api/doctors/${doctorSelect.value}/availableSlots?date=${appointmentDate}`);
      if (!response.ok) {
        throw new Error("Failed to fetch available slots");
      }
      const slots = await response.json();
      populateTimeSlots(slots);
    } catch (error) {
      console.error("Error fetching available slots:", error);
      clearTimeSlots();
    }
  }

  function populateTimeSlots(slots) {
    const appointmentTime = document.getElementById("appointmentTime");
    appointmentTime.innerHTML = '<option value="" disabled selected>Select a time slot</option>';
    slots.forEach(slot => {
      const option = document.createElement("option");
      option.value = slot;
      option.textContent = slot;
      appointmentTime.appendChild(option);
    });
  }

  function clearTimeSlots() {
    const appointmentTime = document.getElementById("appointmentTime");
    appointmentTime.innerHTML = '<option value="" disabled selected>Select a time slot</option>';
  }

  // Form submission
  const bookingForm = document.getElementById("bookingForm");
  if (bookingForm) {
    bookingForm.addEventListener("submit", async (e) => {
      e.preventDefault();

      const date = document.getElementById("appointmentDate").value;
      const time = document.getElementById("appointmentTime").value;
      const dateTime = date && time ? `${date}T${time}` : null;

      const formData = {
        doctorId: document.getElementById("doctorSelect").value,
        patientId: currentPatientId,
        dateTime: dateTime,
        duration: 30,
        appointmentType: document.getElementById("appointmentType").value,
        reasonForVisit: document.getElementById("reasonForVisit").value,
      };

      // Print inputs to console on Book button click
      console.log("Booking form inputs:", {
        date,
        time,
        doctorId: formData.doctorId,
        patientId: formData.patientId,
        dateTime: formData.dateTime,
        duration: formData.duration,
        appointmentType: formData.appointmentType,
        reasonForVisit: formData.reasonForVisit,
      });

      console.log("Booking form data:", formData);

      try {
        const headers = {
          "Content-Type": "application/json",
        };
        if (csrfToken && csrfHeader) {
          headers[csrfHeader] = csrfToken;
        }

        // Changed endpoint from /api/appointments/log to /api/appointments for actual booking
        const response = await fetch("/api/appointments", {
          method: "POST",
          headers: headers,
          body: JSON.stringify(formData),
        });

        if (!response.ok) {
          let errorMessage = "Booking failed";
          try {
            const errorData = await response.json();
            errorMessage = errorData.message || errorMessage;
          } catch (e) {
            // response body empty or invalid JSON
          }
          throw new Error(errorMessage);
        }

        showSuccess("Appointment booked successfully!");
        bookingForm.reset();
        calendar.refetchEvents();
      } catch (error) {
        console.error("Booking error:", error);
        showError(error.message);
      }
    });
  }

  // Add event listeners for Edit and Cancel buttons using event delegation
      const appointmentsList = document.querySelector(".appointments-box ul");
      if (appointmentsList) {
        appointmentsList.addEventListener("click", async (e) => {
          const editButton = e.target.closest(".edit-appointment-btn");
          if (editButton) {
            const appointmentId = editButton.getAttribute("data-appointment-id");
            const newDateTime = prompt("Enter new date and time (YYYY-MM-DDTHH:mm):");
            if (!newDateTime) {
              alert("Change request cancelled.");
              return;
            }
            // Validate newDateTime format: should match YYYY-MM-DD
            const dateTimeRegex = /^\d{4}-\d{2}-\d{2}$/;
            if (!dateTimeRegex.test(newDateTime)) {
              alert("Invalid date format. Please enter in YYYY-MM-DD format.");
              return;
            }

            try {
              const headers = {
                "Content-Type": "application/json",
              };
              if (csrfToken && csrfHeader) {
                headers[csrfHeader] = csrfToken;
              }

              const url = `/api/appointments/${appointmentId}/request-change?requestedDate=${encodeURIComponent(newDateTime)}`;
              const response = await fetch(url, {
                method: "POST",
                headers: headers,
              });

              if (!response.ok) {
                let errorMessage = "Change request failed";
                try {
                  const errorData = await response.json();
                  errorMessage = errorData.message || errorMessage;
                } catch (e) {}
                throw new Error(errorMessage);
              }

              alert("Change request sent successfully.");
              location.reload();
            } catch (error) {
              console.error("Change request error:", error);
              alert(error.message);
            }
          } else if (e.target.closest(".delete-appointment-btn")) {
            const deleteButton = e.target.closest(".delete-appointment-btn");
            const appointmentId = deleteButton.getAttribute("data-appointment-id");
            console.log("Delete button clicked for appointmentId:", appointmentId);

            if (!confirm("Are you sure you want to delete this appointment? This action cannot be undone.")) {
              return;
            }

            try {
              const headers = {
                "Content-Type": "application/json",
              };
              if (csrfToken && csrfHeader) {
                headers[csrfHeader] = csrfToken;
              }

              const url = `/api/appointments/${appointmentId}`;
              const response = await fetch(url, {
                method: "DELETE",
                headers: headers,
              });

              if (!response.ok) {
                let errorMessage = "Deletion failed";
                try {
                  const errorData = await response.json();
                  errorMessage = errorData.message || errorMessage;
                } catch (e) {}
                throw new Error(errorMessage);
              }

              // Remove appointment from Upcoming Appointments list
              const appointmentListItem = document.querySelector(`.appointments-box ul li[data-appointment-id="${appointmentId}"]`);
              console.log("Appointment list item found:", appointmentListItem);
              if (appointmentListItem) {
                appointmentListItem.remove();
              } else {
                console.warn("Appointment list item not found for id:", appointmentId);
              }

              // Remove appointment from FullCalendar
              let event = calendar.getEventById(appointmentId);
              if (!event) {
                // Fallback: find event by matching extendedProps.appointmentId or other property
                event = calendar.getEvents().find(ev => ev.extendedProps && ev.extendedProps.appointmentId == appointmentId);
              }
              console.log("FullCalendar event found:", event);
              if (event) {
                event.remove();
                calendar.refetchEvents();
              } else {
                console.warn("FullCalendar event not found for id:", appointmentId);
                console.log("Current calendar events:", calendar.getEvents());
              }

              alert("Appointment deleted successfully.");
            } catch (error) {
              console.error("Deletion error:", error);
              alert(error.message);
            }
          }
        });
      }

  // Initialize
  fetchDoctors();

  function showSuccess(message) {
    const msgEl = document.getElementById("bookingMessage");
    msgEl.textContent = message;
    msgEl.className = "mt-3 text-success";
  }

  function showError(message) {
    const msgEl = document.getElementById("bookingMessage");
    msgEl.textContent = message;
    msgEl.className = "mt-3 text-danger";
  }
});
