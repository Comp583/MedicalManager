const CalendarManager = (function () {
  function getDayOfWeek(dateString) {
    const days = [
      "Sunday",
      "Monday",
      "Tuesday",
      "Wednesday",
      "Thursday",
      "Friday",
      "Saturday",
    ];
    const date = new Date(dateString);
    return days[date.getDay()];
  }

  function formatTime(time24) {
    const [hourStr, minute] = time24.split(":");
    let hour = parseInt(hourStr, 10);
    const ampm = hour >= 12 ? "PM" : "AM";
    hour = hour % 12 || 12;
    return `${hour}:${minute} ${ampm}`;
  }

  let events = [];

  const listeners = [];

  async function fetchEventsFromApi() {
    try {
      const response = await fetch("/api/appointments");
      if (!response.ok) {
        throw new Error("Failed to fetch appointments");
      }
      const data = await response.json();
      events = data.map((item) => {
        const start = item.dateTime;
        const endDate = new Date(
          new Date(start).getTime() + (item.duration || 30) * 60000
        );
        return {
          id: item.id,
          title: `${item.patientName} - Appointment - ${formatTime(
            start.substring(11, 16)
          )}`,
          start: start,
          end: endDate.toISOString(),
          patientName: item.patientName,
          time: start.substring(11, 16),
          day: start.substring(0, 10),
          duration: item.duration,
          status: item.status,
          appointmentType: item.appointmentType,
          reasonForVisit: item.reasonForVisit,
          notes: item.notes,
        };
      });
      notifyListeners();
    } catch (error) {
      console.error("Error fetching appointments:", error);
    }
  }

  async function updateEvent(id, newData) {
    try {
      const newDateTime = newData.day + "T" + newData.time + ":00";
      const response = await fetch(`/api/appointments/${id}`, {
        method: "PUT",
        headers: {
          "Content-Type": "application/json",
        },
        credentials: "include",
        body: JSON.stringify({ newDateTime }),
      });
      if (!response.ok) {
        throw new Error("Failed to update appointment");
      }
      const updated = await response.json();
      // Update local events array
      const index = events.findIndex((e) => e.id === id);
      if (index !== -1) {
        events[index] = {
          ...events[index],
          id: updated.id,
          title: `${updated.patientName} - Appointment - ${formatTime(
            updated.dateTime.substring(11, 16)
          )}`,
          start: updated.dateTime,
          end: new Date(
            new Date(updated.dateTime).getTime() +
              (updated.duration || 30) * 60000
          ).toISOString(),
          patientName: updated.patientName,
          time: updated.dateTime.substring(11, 16),
          day: updated.dateTime.substring(0, 10),
          duration: updated.duration,
          status: updated.status,
          appointmentType: updated.appointmentType,
          reasonForVisit: updated.reasonForVisit,
          notes: updated.notes,
        };
        notifyListeners();
        return true;
      }
      return false;
    } catch (error) {
      console.error("Error updating appointment:", error);
      return false;
    }
  }

  function getEvents() {
    return events;
  }

  function addListener(callback) {
    if (typeof callback === "function") {
      listeners.push(callback);
    }
  }

  function notifyListeners() {
    listeners.forEach((callback) => callback(events));
  }

  return {
    getEvents,
    updateEvent,
    addListener,
    getDayOfWeek,
    formatTime,
    fetchEventsFromApi,
  };
})();

export default CalendarManager;
