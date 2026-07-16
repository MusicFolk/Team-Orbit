document.addEventListener("DOMContentLoaded", function () {

    const toggle = document.getElementById("chat-toggle");
    const windowBox = document.getElementById("chat-window");
    const answer = document.getElementById("chat-answer");

    if (!toggle || !windowBox || !answer) {
        console.error("Chatbot elements not found.");
        return;
    }

    toggle.addEventListener("click", function () {

        if (windowBox.style.display === "block") {
            windowBox.style.display = "none";
        } else {
            windowBox.style.display = "block";
        }

    });

    const replies = {

        "How do I register?":
            "Click the Get Started button and complete the registration form.",

        "How do I create an event?":
            "After logging in, click Create Event and fill in the event details.",

        "How do I join an event?":
            "Open an event and click RSVP or Join Event.",

        "How do I edit an event?":
            "Open My Events and click Edit.",

        "How do I cancel my RSVP?":
            "Open the event and click Cancel RSVP.",

        "How do I comment?":
            "Open any event and scroll to the Comments section.",

        "Can I search events?":
            "Yes. Use the search bar and filters.",

        "Can I edit my profile?":
            "Yes. Open your profile and click Edit Profile.",

        "Who can create events?":
            "Any registered user can create events.",

        "Contact support":
            "Contact the Orbit administrator for assistance."

    };

    document.querySelectorAll(".question").forEach(function (button) {

        button.addEventListener("click", function () {

            answer.innerHTML =
                "<b>You:</b> " + this.innerText +
                "<br><br><b>Orbit Assistant:</b><br>" +
                replies[this.innerText];

        });

    });

});