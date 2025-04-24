// login_handler.js
document.addEventListener('DOMContentLoaded', () => {
  const form = document.querySelector('form');

  // Temporary users list for simulation
  const users = [
    { username: 'tempclient', password: 'client123', role: 'patient' },
    { username: 'tempdoctor', password: 'doctor123', role: 'doctor' },
    { username: 'tempadmin', password: 'admin123', role: 'admin' }
  ];

  form.addEventListener('submit', (event) => {
    event.preventDefault();

    const username = document.getElementById('username').value.trim().toLowerCase();
    const password = document.getElementById('password').value;

    // Find user in temporary users list
    const user = users.find(u => u.username === username && u.password === password);

    if (user) {
      // Redirect based on role
      if (user.role === 'admin') {
        window.location.href = 'AdminLanding.html';
      } else if (user.role === 'doctor') {
        window.location.href = 'DrLandingPg.html';
      } else {
        window.location.href = 'ClientLandingPage.html';
      }
    } else {
      alert('Invalid username or password. Please try again.');
    }
  });
});
