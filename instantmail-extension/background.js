chrome.runtime.onMessage.addListener((request, sender, sendResponse) => {
  if (request.action === "generateEmail") {
    fetch('https://instantmail.shop/api/email/generate', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'X-Extension-Id': chrome.runtime.id
      },
      body: JSON.stringify(request.data)
    })
    .then(response => {
      if (!response.ok) throw new Error(`HTTP error! status: ${response.status}`);
      return response.text();
    })
    .then(text => sendResponse({ success: true, text }))
    .catch(error => sendResponse({ success: false, error: error.message }));

    return true;
  }
});