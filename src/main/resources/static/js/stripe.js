const stripe = Stripe('pk_test_51Q9M36G1Q8EB8XUa1JzgsvmXZCtUyDFLeJozJOM78A4gjNbfUNZ3ITbwn0gQOjvCoQYnDxYzPdI2oSpjtQqZsnlp00N7ic3SNK');

const subscribeButton = document.querySelector('#subscribeButton');
const csrfToken = document.querySelector('input[name="_csrf"]').value;

subscribeButton.addEventListener('click', async (event) => {
    event.preventDefault(); // Prevent default form submission
    
    const response = await fetch('/user/create-checkout-session', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
            // Include CSRF token if needed
             'X-CSRF-Token': csrfToken 
        },
        body: JSON.stringify({
            priceId: 'price_1Q9M3bG1Q8EB8XUacUbkFtmb' 
        })
    });

    if (!response.ok) {
        const error = await response.text(); // Get error response
        console.error('Error:', error); // Log error
        alert('Failed to create checkout session. ' + error);
        return;
    }

    const { sessionId } = await response.json(); // Get sessionId from server response

    const result = await stripe.redirectToCheckout({
        sessionId: sessionId
    });

    if (result.error) {
        console.error("Error:", result.error);
        // Handle the error appropriately
    } else {
        // Redirect to Stripe checkout
        const sessionId = result.sessionId;
        const stripe = Stripe('pk_test_51Q9M36G1Q8EB8XUa1JzgsvmXZCtUyDFLeJozJOM78A4gjNbfUNZ3ITbwn0gQOjvCoQYnDxYzPdI2oSpjtQqZsnlp00N7ic3SNK');
        stripe.redirectToCheckout({ sessionId: sessionId });
    }
});