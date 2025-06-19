// 🚀 Handle form submission
document.querySelector("form").addEventListener("submit", function (event) {
  event.preventDefault();

  const prompt = document.querySelector("textarea[name='prompt']").value.trim();

  if (!prompt) {
    alert("Please enter a prompt.");
    return;
  }

  fetch("/generate", {
    method: "POST",
    headers: { "Content-Type": "application/x-www-form-urlencoded" },
    body: new URLSearchParams({ prompt })
  })
    .then(res => res.text())
    .then(text => {
      if (text === "success") {
        window.location.href = "/view";
      } else {
        alert("Server error: " + text);
      }
    })
    .catch(err => {
      alert("There was an error submitting your request.");
      console.error(err);
    });
});

// 🔄 Dynamic rotating text
const words = ["website", "prototype", "landing page"];
let currentIndex = 0;
setInterval(() => {
  currentIndex = (currentIndex + 1) % words.length;
  const wordElement = document.getElementById("dynamic-word");
  if (wordElement) {
    wordElement.textContent = words[currentIndex];
  }
}, 3000);

// 👤 Profile toggle logic
document.addEventListener("DOMContentLoaded", () => {
  const userIcon = document.querySelector(".user-profile");
  const profileCard = document.querySelector(".profile-card");

  if (!userIcon || !profileCard) return;

  userIcon.addEventListener("click", (e) => {
    e.stopPropagation();
    profileCard.style.display = profileCard.style.display === "block" ? "none" : "block";
  });

  document.addEventListener("click", (e) => {
    if (!userIcon.contains(e.target) && !profileCard.contains(e.target)) {
      profileCard.style.display = "none";
    }
  });
});

  function showLoader() {
    document.getElementById("loader-overlay").style.display = "flex";
  }