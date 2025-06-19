   const form = document.querySelector("form");
        form.addEventListener("submit", function (event) {
            event.preventDefault();
            const prompt = document.querySelector("textarea[name='prompt']").value;

            if (prompt.trim() === "") {
                alert("Please enter a prompt.");
            } else {
                form.submit();
            }
        });

       document.addEventListener('DOMContentLoaded', function() {
    const userIcon = document.querySelector('.user-profile');  // Update to the correct class for user icon
    const profileCard = document.querySelector('.profile-card');

    userIcon.addEventListener('click', function(event) {
        // Prevent closing the card when clicking inside the user profile
        event.stopPropagation();
        
        // Toggle the display of the profile card
        profileCard.style.display = profileCard.style.display === 'block' ? 'none' : 'block';
    });

    // Close the profile card if the user clicks outside of it
    document.addEventListener('click', function(event) {
        if (!userIcon.contains(event.target) && !profileCard.contains(event.target)) {
            profileCard.style.display = 'none';
        }
    });
});