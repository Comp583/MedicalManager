// Doctor Schema
db.createCollection("doctors", {
  validator: {
    $jsonSchema: {
      bsonType: "object",
      required: ["username","firstName","lastName","medicalId","biography"],
      properties: {
        username: {
          bsonType: "string",
          description: "Doctor's username for login - must be unique"
        },
        firstName: {
          bsonType: "string",
          description: "Doctor's first name"
        },
        lastName: {
          bsonType: "string",
          description: "Doctor's last name"
        },
        medicalId: {
          bsonType: "string",
          description: "Doctor's unique medical identification number"
        },
        bio: {
          bsonType: "string",
          description: "Summary of doctor's professional background"
        },
        availableSlots: {
          bsonType: "array",
          description: "Doctor's available time slots",
          items: {
            bsonType: "object",
            required: ["dayOfWeek","startTime","endTime"],
            properties: {
              dayOfWeek: {
                bsonType: "string",
                enum: [
                  "MONDAY","TUESDAY","WEDNESDAY",
                  "THURSDAY","FRIDAY","SATURDAY","SUNDAY"
                ],
                description: "Day of the week"
              },
              startTime: {
                bsonType: "string",
                pattern: "^(?:[01]\\d|2[0-3]):[0-5]\\d$",
                description: "Start time in HH:MM (24h) format"
              },
              endTime: {
                bsonType: "string",
                pattern: "^(?:[01]\\d|2[0-3]):[0-5]\\d$",
                description: "End time in HH:MM (24h) format"
              }
            }
          }
        }
      }
    }
  }
});
db.doctors.createIndex({ username: 1 }, { unique: true });
db.doctors.createIndex({ medicalId: 1 }, { unique: true });
  
  // Appointments Collection
  db.createCollection("appointments", {
    validator: {
      $jsonSchema: {
        bsonType: "object",
        required: ["doctorId", "patientId", "dateTime", "duration", "status"],
        properties: {
          doctorId: {
            bsonType: "objectId",
            description: "Reference to the doctor"
          },
          patientId: {
            bsonType: "objectId", 
            description: "Reference to the patient"
          },
          dateTime: {
            bsonType: "date",
            description: "Date and time of the appointment"
          },
          duration: {
            bsonType: "int",
            description: "Duration of appointment in minutes"
          },
          status: {
            bsonType: "string",
            enum: ["scheduled", "completed", "cancelled", "rescheduled"],
            description: "Current status of the appointment"
          },
          appointmentType: {
            bsonType: "string",
            description: "Type of appointment (e.g., check-up, follow-up, consultation)"
          },
          notes: {
            bsonType: "string",
            description: "Additional notes regarding the appointment"
          },
          reasonForVisit: {
            bsonType: "string",
            description: "Patient's reason for visit"
          },
          previousAppointmentId: {
            bsonType: "objectId",
            description: "Reference to previous appointment if this is a rescheduled appointment"
          }
        }
      }
    }
  });
  
  // Create indexes for efficient querying
  db.appointments.createIndex({ "doctorId": 1, "dateTime": 1 });
  db.appointments.createIndex({ "patientId": 1, "dateTime": 1 });
  db.appointments.createIndex({ "status": 1 });
  
  // Admin Users Collection
  db.createCollection("admins", {
    validator: {
      $jsonSchema: {
        bsonType: "object",
        required: ["username", "passwordHash", "fullName", "role", "permissions"],
        properties: {
          username: {
            bsonType: "string",
            description: "Admin username - must be unique"
          },
          passwordHash: {
            bsonType: "string",
            description: "Hashed password using bcrypt"
          },
          fullName: {
            bsonType: "string",
            description: "Admin's full name"
          },
          role: {
            bsonType: "string",
            enum: ["superadmin", "admin"],
            description: "Admin role level"
          },
          permissions: {
            bsonType: "array",
            description: "List of permissions",
            items: {
              bsonType: "string",
              enum: ["view_all", "edit_all", "manage_doctors", "manage_patients"]
            }
          },
          lastLogin: {
            bsonType: "date",
            description: "Last login timestamp"
          }
        }
      }
    }
  });

  // Create unique index on username
  db.admins.createIndex({ "username": 1 }, { unique: true });

  // Patients Collection
  db.createCollection("patients", {
    validator: {
      $jsonSchema: {
        bsonType: "object",
        required: ["username", "password", "fullName", "patientId", "email address"],
        properties: {
          username: {
            bsonType: "string",
            description: "Patient's username - must be unique"
          },
          password: {
            bsonType: "string",
            description: "Patient's password (stored as plaintext per requirements)"
          },
          fullName: {
            bsonType: "object",
            required: ["firstName", "lastName"],
            properties: {
              firstName: {
                bsonType: "string",
                description: "Patient's first name"
              },
              lastName: {
                bsonType: "string",
                description: "Patient's last name"
              }
            }
          },
          patientId: {
            bsonType: "string",
            description: "Patient's unique medical identifier"
          },
          patientEmail: {
            bsonType: "string",
            description: "Patient's email address"
          },
          createdAt: {
            bsonType: "date",
            description: "When account was created"
          }
        }
      }
    }
  });

  // Create unique indexes
  db.patients.createIndex({ "username": 1 }, { unique: true });
  db.patients.createIndex({ "patientId": 1 }, { unique: true });

  // Audit Log Collection
  db.createCollection("audit_log", {
    validator: {
      $jsonSchema: {
        bsonType: "object",
        required: ["adminId", "action", "timestamp"],
        properties: {
          adminId: {
            bsonType: "objectId",
            description: "Reference to admin user"
          },
          action: {
            bsonType: "string",
            description: "Action performed"
          },
          timestamp: {
            bsonType: "date",
            description: "When action occurred"
          },
          details: {
            bsonType: "object",
            description: "Additional action details"
          },
          targetCollection: {
            bsonType: "string",
            description: "Which collection was affected"
          },
          targetId: {
            bsonType: "objectId",
            description: "ID of affected document"
          }
        }
      }
    }
  });

  // Insert 3 admin users (passwords should be hashed in production)
  db.admins.insertMany([
    {
      username: "nic_admin",
      passwordHash: "$2a$10$N9qo8uLOickgx2ZMRZoMy.Mrq4W7.ZBS1LmYgR5wJYVY1JQo9Q5qK", // hash for "Nic123!"
      fullName: "Nic Admin",
      role: "superadmin",
      permissions: ["view_all", "edit_all", "manage_doctors", "manage_patients"],
      lastLogin: null
    },
    {
      username: "jess_admin",
      passwordHash: "$2a$10$N9qo8uLOickgx2ZMRZoMy.Mrq4W7.ZBS1LmYgR5wJYVY1JQo9Q5qK", // hash for "Jess123!"
      fullName: "Jess Admin",
      role: "admin",
      permissions: ["view_all", "edit_all"],
      lastLogin: null
    },
    {
      username: "angel_admin",
      passwordHash: "$2a$10$N9qo8uLOickgx2ZMRZoMy.Mrq4W7.ZBS1LmYgR5wJYVY1JQo9Q5qK", // hash for "Angel123!"
      fullName: "Angel Admin",
      role: "admin",
      permissions: ["view_all", "edit_all"],
      lastLogin: null
    }
  ]);

  // Insert sample doctors
  db.doctors.insertMany([
    {
      username: "dr.smith",
      legalName: {
        firstName: "John",
        lastName: "Smith"
      },
      medicalId: "MD12345",
      biography: "Dr. Smith is a board-certified cardiologist with over 15 years of experience in treating various heart conditions.",
      availableSlots: [
        { dayOfWeek: "Monday", startTime: "09:00", endTime: "17:00" },
        { dayOfWeek: "Wednesday", startTime: "09:00", endTime: "17:00" },
        { dayOfWeek: "Friday", startTime: "09:00", endTime: "15:00" }
      ],
      /*breakTimes: [
        { dayOfWeek: "Monday", startTime: "12:00", endTime: "13:00" },
        { dayOfWeek: "Wednesday", startTime: "12:00", endTime: "13:00" },
        { dayOfWeek: "Friday", startTime: "12:00", endTime: "13:00" }
      ],
      contactInfo: {
        email: "john.smith@medicalmanager.com",
        phone: "555-123-4567"
      }*/
    },
    {
      username: "dr.patel",
      legalName: {
        firstName: "Riya",
        lastName: "Patel"
      },
      medicalId: "MD67890",
      biography: "Dr. Patel specializes in pediatric care with a focus on early childhood development and preventive healthcare.",
      availableSlots: [
        { dayOfWeek: "Tuesday", startTime: "08:00", endTime: "16:00" },
        { dayOfWeek: "Thursday", startTime: "08:00", endTime: "16:00" },
        { dayOfWeek: "Saturday", startTime: "10:00", endTime: "14:00" }
      ],
      /*breakTimes: [
        { dayOfWeek: "Tuesday", startTime: "12:00", endTime: "13:00" },
        { dayOfWeek: "Thursday", startTime: "12:00", endTime: "13:00" }
      ],
      contactInfo: {
        email: "riya.patel@medicalmanager.com",
        phone: "555-789-0123"
      }*/
    },
    {
      username: "dr.johnson",
      legalName: {
        firstName: "Michael",
        lastName: "Johnson"
      },
      medicalId: "MD54321",
      biography: "Dr. Johnson is an orthopedic surgeon specializing in sports medicine and joint replacements with over 20 years of experience.",
      availableSlots: [
        { dayOfWeek: "Monday", startTime: "10:00", endTime: "18:00" },
        { dayOfWeek: "Thursday", startTime: "10:00", endTime: "18:00" },
        { dayOfWeek: "Friday", startTime: "10:00", endTime: "16:00" }
      ],
      /*breakTimes: [
        { dayOfWeek: "Monday", startTime: "13:00", endTime: "14:00" },
        { dayOfWeek: "Thursday", startTime: "13:00", endTime: "14:00" },
        { dayOfWeek: "Friday", startTime: "13:00", endTime: "14:00" }
      ],
      contactInfo: {
        email: "michael.johnson@medicalmanager.com",
        phone: "555-456-7890"
      }*/
    },
    {
      username: "dr.garcia",
      legalName: {
        firstName: "Elena",
        lastName: "Garcia"
      },
      medicalId: "MD98765",
      biography: "Dr. Garcia is a dermatologist with expertise in skin cancer detection and cosmetic dermatology procedures.",
      availableSlots: [
        { dayOfWeek: "Tuesday", startTime: "09:00", endTime: "17:00" },
        { dayOfWeek: "Wednesday", startTime: "09:00", endTime: "17:00" },
        { dayOfWeek: "Friday", startTime: "09:00", endTime: "15:00" }
      ],
      /*breakTimes: [
        { dayOfWeek: "Tuesday", startTime: "12:30", endTime: "13:30" },
        { dayOfWeek: "Wednesday", startTime: "12:30", endTime: "13:30" },
        { dayOfWeek: "Friday", startTime: "12:30", endTime: "13:30" }
      ],
      contactInfo: {
        email: "elena.garcia@medicalmanager.com",
        phone: "555-234-5678"
      }*/
    },
  ]);