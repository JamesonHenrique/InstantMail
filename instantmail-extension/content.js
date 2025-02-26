console.log("InstantMail - Script de Conteúdo Carregado");

function createAIButton() {
   const button = document.createElement('div');
   button.className = 'T-I J-J5-Ji aoO v7 T-I-atl L3';
   button.style.marginRight = '8px';
   button.innerHTML = 'Resposta de IA';
   button.style.borderRadius = '18px';
   button.style.backgroundColor = '#0B57D0';
   button.style.color = 'white'

   button.setAttribute('role','button');
   button.setAttribute('data-tooltip','InstantMail - Gerar Resposta de IA');
   return button;
}
function createToneSelector() {
    const select = document.createElement('select');
    select.className = 'tone-selector';
    

    select.style.marginRight = '8px';
    select.style.padding = '7px'; 
    select.style.borderRadius = '18px'; 
    select.style.border = '1px solid white'; 
    select.style.backgroundColor = '#0B57D0'; 
    select.style.color = 'white'; 
    select.style.fontSize = '15px'; 

    const tones = ['professional', 'friendly', 'urgent', 'casual'];
    tones.forEach(tone => {
        const option = document.createElement('option');
        option.value = tone;
        option.textContent = tone.charAt(0).toUpperCase() + tone.slice(1);
        select.appendChild(option);
    });

    return select;
}
function getEmailContent() {
    const selectors = [
        '.h7',
        '.a3s.aiL',
        '.gmail_quote',
        '[role="presentation"]'
    ];
    for (const selector of selectors) {
        const content = document.querySelector(selector);
        if (content) {
            return content.innerText.trim();
        }
        return '';
    }
}


function findComposeToolbar() {
    const selectors = [
        '.btC',
        '.aDh',
        '[role="toolbar"]',
        '.gU.Up'
    ];
    for (const selector of selectors) {
        const toolbar = document.querySelector(selector);
        if (toolbar) {
            return toolbar;
        }
        return null;
    }
}

function injectButton() {
    const existingButton = document.querySelector('.ai-reply-button');
    if (existingButton) existingButton.remove();

    const toolbar = findComposeToolbar();
    if (!toolbar) {
        console.log("Toolbar não encontrada");
        return;
    }

    console.log("Toolbar encontrada, criando botão de IA");
    const button = createAIButton();
    button.classList.add('ai-reply-button');
    const toneSelector = createToneSelector();
    toolbar.insertBefore(toneSelector, toolbar.firstChild); 

    button.addEventListener('click', async () => {
        try {
            button.innerHTML = 'Gerando...';
            button.disabled = true;

            const emailContent = getEmailContent();
             const selectedTone = toneSelector.value; // O
            const response = await fetch('http://localhost:8080/api/email/generate', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({
                    emailContent: emailContent,
                    tone: selectedTone
                })
            });

            if (!response.ok) {
                throw new Error('Falha na requisição da API');
            }

            const generatedReply = await response.text();
            const composeBox = document.querySelector('[role="textbox"][g_editable="true"]');

            if (composeBox) {
                composeBox.focus();
                document.execCommand('insertText', false, generatedReply);
            } else {
                console.error('Caixa de composição não foi encontrada');
            }
        } catch (error) {
            console.error(error);
            alert('Falha ao gerar resposta');
        } finally {
            button.innerHTML = 'Resposta de IA';
            button.disabled =  false;
        }
    });

    toolbar.insertBefore(button, toolbar.firstChild);
}

const observer = new MutationObserver((mutations) => {
    for(const mutation of mutations) {
        const addedNodes = Array.from(mutation.addedNodes);
        const hasComposeElements = addedNodes.some(node =>
            node.nodeType === Node.ELEMENT_NODE && 
            (node.matches('.aDh, .btC, [role="dialog"]') || node.querySelector('.aDh, .btC, [role="dialog"]'))
        );

        if (hasComposeElements) {
            console.log("Janela de Composição Detectada");
            setTimeout(injectButton, 500);
        }
    }
});


observer.observe(document.body, {
    childList: true,
    subtree: true
});