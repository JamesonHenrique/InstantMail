console.log("InstantMail - Script de Conteúdo Carregado");

function createAIButton() {
    const button = document.createElement('div');
    button.className = 'T-I J-J5-Ji aoO v7 T-I-atl L3 ai-reply-button';
    button.style.marginRight = '8px';
    button.innerHTML = 'Resposta de IA';
    button.style.borderRadius = '18px';
    button.style.backgroundColor = '#0B57D0';
    button.style.color = 'white';
    button.style.padding = '0 16px';
    button.style.cursor = 'pointer';
    button.style.display = 'inline-flex';
    button.style.alignItems = 'center';
    button.style.justifyContent = 'center';
    button.style.height = '36px';

    button.setAttribute('role', 'button');
    button.setAttribute('data-tooltip', 'InstantMail - Gerar Resposta de IA');
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
    select.style.fontSize = '14px';
    select.style.height = '36px';
    select.style.cursor = 'pointer';

    const tones = [
        { value: 'professional', text: 'Profissional' },
        { value: 'friendly', text: 'Amigável' },
        { value: 'urgent', text: 'Urgente' },
        { value: 'casual', text: 'Casual' }
    ];

    tones.forEach(tone => {
        const option = document.createElement('option');
        option.value = tone.value;
        option.textContent = tone.text;
        select.appendChild(option);
    });

    return select;
}

function getEmailContent() {
    const emailSelectors = [
        '.ii.gt',
        '.a3s.aiL',
        '[role="presentation"]',
        '.gs',
        '.adn.ads',
        '.msg-8223721542020609',
        '.email_body',
        '.e4XSEd',
        '.ii.gt.adO.aj8',
        '.gmail_quote'
    ];
    for (const selector of emailSelectors) {
        const element = document.querySelector(selector);
        if (element && element.innerText && element.innerText.trim().length > 50) {
            return element.innerText.trim();
        }
    }

    const emailContainers = [
        document.querySelector('.nH.if'),
        document.querySelector('.a3s')
    ];

    for (const container of emailContainers) {
        if (container) {
            let text = container.innerText;

            text = text.replace(/--\s*[\s\S]*$/gm, '');
            text = text.replace(/On\s.*wrote:[\s\S]*$/gm, '');
            text = text.replace(/Em\s.*escreveu:[\s\S]*$/gm, '');

            if (text.trim().length > 50) {
                return text.trim();
            }
        }
    }

    const allText = document.body.innerText;
    const lines = allText.split('\n')
        .filter(line => line.trim().length > 0)
        .filter(line => !line.includes('Forwarded message'))
        .filter(line => !line.includes('Mensagem encaminhada'))
        .filter(line => !line.includes('---------- Forwarded message ---------'))
        .filter(line => !line.match(/^On\s.+\s<\w+@\w+\.\w+>\s+wrote:$/))
        .filter(line => !line.match(/^De:\s.+/))
        .filter(line => !line.match(/^Para:\s.+/))
        .filter(line => !line.match(/^Data:\s.+/))
        .filter(line => !line.match(/^Assunto:\s.+/));

    const filteredText = lines.join('\n').trim();
    return filteredText.length > 50 ? filteredText : '';
}
function debugEmailContent() {
    console.log("=== DEBUG DE ELEMENTOS DE E-MAIL ===");

    document.querySelectorAll('*').forEach(el => {
        if (el.innerText && el.innerText.trim().length > 20 &&
            !el.innerText.includes('http') &&
            !el.tagName.match(/SCRIPT|STYLE|LINK|META/)) {
            console.log(`Tag: ${el.tagName}, Classes: ${el.className}, Text: ${el.innerText.substring(0, 50)}...`);
        }
    });

}
function findComposeToolbar() {
    const selectors = [
        '.gU.Up',
        '.btC',
        '.aDh',
        '[role="toolbar"]',
        '.G3.G2',
        '.J-J5-Ji'
    ];

    for (const selector of selectors) {
        const toolbar = document.querySelector(selector);
        if (toolbar) {
            return toolbar;
        }
    }
    return null;
}

async function generateAIResponse(emailContent, tone) {
    try {
        const response = await fetch('https://instantmail.shop/api/email/generate', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({
                emailContent: emailContent,
                tone: tone
            })
        });

        if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
        }

        return await response.text();
    } catch (error) {
        console.error('Erro na requisição:', error);
        throw error;
    }
}

function insertTextIntoComposeBox(text) {
    const composeBox = document.querySelector('[role="textbox"][g_editable="true"], .editable.LW-avf');

    if (composeBox) {
        composeBox.focus();
        document.execCommand('selectAll', false, null);
        document.execCommand('delete', false, null);
        document.execCommand('insertText', false, text);
        return true;
    }

    console.error('Caixa de composição não encontrada');
    return false;
}

function injectButton() {
    document.querySelectorAll('.ai-reply-button, .tone-selector').forEach(el => el.remove());

    const toolbar = findComposeToolbar();
    if (!toolbar) {
        console.log("Toolbar não encontrada - tentando novamente em 500ms");
        setTimeout(injectButton, 500);
        return;
    }

    console.log("Toolbar encontrada, criando botão de IA");
    const button = createAIButton();
    const toneSelector = createToneSelector();

    if (toolbar.firstChild) {
        toolbar.insertBefore(toneSelector, toolbar.firstChild);
        toolbar.insertBefore(button, toneSelector.nextSibling);
    } else {
        toolbar.appendChild(toneSelector);
        toolbar.appendChild(button);
    }

   button.addEventListener('click', async () => {
       try {
           button.innerHTML = '<span>Gerando...</span>';
           button.style.opacity = '0.7';
           button.disabled = true;

           const emailContent = getEmailContent();

           if (!emailContent || emailContent.length < 10) {
               console.log("Conteúdo capturado:", emailContent);
               console.log("Elementos encontrados:", document.querySelectorAll('*'));

               throw new Error(`Não foi possível obter o conteúdo do e-mail. Por favor, certifique-se que você está visualizando um e-mail aberto ou tentando responder a uma mensagem.`);
           }

           const selectedTone = toneSelector.value;
           const generatedReply = await generateAIResponse(emailContent, selectedTone);

           if (!insertTextIntoComposeBox(generatedReply)) {
               throw new Error('Não foi possível inserir o texto na caixa de composição. Por favor, certifique-se que a janela de composição está aberta.');
           }
       } catch (error) {
           console.error('Erro:', error);

           if (error.message.includes('conteúdo do e-mail')) {
               alert('Não conseguimos ler o e-mail. Por favor:\n1. Abra o e-mail que deseja responder\n2. Clique no botão de resposta\n3. Tente novamente');
           } else {
               alert('Erro: ' + error.message);
           }
       } finally {
           button.innerHTML = '<span>Resposta de IA</span>';
           button.style.opacity = '1';
           button.disabled = false;
       }
   });
}

const observer = new MutationObserver((mutations) => {
    mutations.forEach((mutation) => {
        if (!mutation.addedNodes.length) return;

        mutation.addedNodes.forEach((node) => {
            if (node.nodeType === Node.ELEMENT_NODE) {
                if (node.matches('[role="dialog"], .nH, .no, .nn') ||
                    node.querySelector('[role="dialog"], .btC, .aDh, [role="toolbar"]')) {
                    console.log("Elemento de composição detectado");
                    setTimeout(injectButton, 300);
                }
            }
        });
    });
});

observer.observe(document.body, {
    childList: true,
    subtree: true,
    attributes: false,
    characterData: false
});

setTimeout(injectButton, 1000);