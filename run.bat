@echo off
set DB_URL=jdbc:postgresql://localhost:5432/leave_management_db
set DB_USERNAME=postgres
set DB_PASSWORD=your_actual_password

echo Starting Leave Management System with database connection...
echo DB_URL: %DB_URL%
echo DB_USERNAME: %DB_USERNAME%

mvn spring-boot:run 