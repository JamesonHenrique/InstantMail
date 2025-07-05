const CONFIG = {
  API_ENDPOINT: 'https://instantmail.shop/api/email/generate',
  BUTTON_TEXT: {
    DEFAULT: 'Resposta de IA',
    LOADING: 'Gerando...',
    ERROR: 'Erro! Tentar novamente'
  },
  TONES: [
    { value: 'professional', label: 'Profissional' },
    { value: 'friendly', label: 'Amigável' },
    { value: 'urgent', label: 'Urgente' },
    { value: 'casual', label: 'Casual' }
  ],
  STYLES: {
    BUTTON: {
      marginRight: '8px',
      borderRadius: '18px',
      backgroundColor: '#0B57D0',
      color: 'white',
      padding: '0 16px',
      height: '36px',
      minWidth: '120px'
    },
    SELECT: {
      marginRight: '8px',
      padding: '7px',
      borderRadius: '18px',
      border: '1px solid white',
      backgroundColor: '#0B57D0',
      color: 'white',
      fontSize: '15px',
      height: '36px'
    }
  },
  SELECTORS: {
    EMAIL_CONTENT: ['.h7', '.a3s.aiL', '.gmail_quote', '[role="presentation"]'],
    COMPOSE_TOOLBAR: ['.btC', '.aDh', '[role="toolbar"]', '.gU.Up'],
    COMPOSE_BOX: '[role="textbox"][g_editable="true"]'
  }
};

class DOMUtils {
  static createElement(tag, attributes = {}, styles = {}) {
    const element = document.createElement(tag);
    Object.entries(attributes).forEach(([key, value]) => element.setAttribute(key, value));
    Object.assign(element.style, styles);
    return element;
  }

  static findFirstElement(selectors, parent = document) {
    for (const selector of selectors) {
      const element = parent.querySelector(selector);
      if (element) return element;
    }
    return null;
  }
}

class AIComponents {
  static createAIButton() {
    const button = DOMUtils.createElement('div', {
      'class': 'T-I J-J5-Ji aoO v7 T-I-atl L3 ai-reply-button',
      'role': 'button',
      'data-tooltip': 'InstantMail - Gerar Resposta de IA',
      'aria-label': 'Gerar resposta com IA'
    }, CONFIG.STYLES.BUTTON);

    button.textContent = CONFIG.BUTTON_TEXT.DEFAULT;
    return button;
  }

  static createToneSelector() {
    const select = DOMUtils.createElement('select', {
      'class': 'tone-selector',
      'aria-label': 'Selecionar tom da resposta'
    }, CONFIG.STYLES.SELECT);

    CONFIG.TONES.forEach(tone => {
      const option = DOMUtils.createElement('option', { value: tone.value });
      option.textContent = tone.label;
      select.appendChild(option);
    });

    return select;
  }
}

class InstantMailExtension {
  constructor() {
    this.observer = null;
    this.initialized = false;
    this.init();
  }

  init() {
    if (this.initialized) return;
    this.initialized = true;

    console.log('InstantMail - Extensão inicializada');
    this.setupMutationObserver();
    this.injectStyles();
  }

  injectStyles() {
    const style = DOMUtils.createElement('style');
    style.textContent = `
      .ai-reply-button:hover {
        background-color: #1b6de0 !important;
        box-shadow: 0 1px 2px 0 rgba(26,115,232,0.45);
      }
      .tone-selector:hover {
        background-color: #1b6de0 !important;
      }
      .tone-selector:focus {
        outline: 2px solid #8ab4f8;
      }
    `;
    document.head.appendChild(style);
  }

  setupMutationObserver() {
    this.observer = new MutationObserver(mutations => {
      mutations.forEach(mutation => {
        const hasComposeElements = Array.from(mutation.addedNodes).some(node =>
          node.nodeType === Node.ELEMENT_NODE &&
          (node.matches('.aDh, .btC, [role="dialog"]') ||
           node.querySelector('.aDh, .btC, [role="dialog"]'))
        );

        if (hasComposeElements) {
          this.debouncedInjectButton();
        }
      });
    });

    this.observer.observe(document.body, {
      childList: true,
      subtree: true
    });
  }

  debouncedInjectButton = this.debounce(() => this.injectButton(), 500);

  debounce(func, wait) {
    let timeout;
    return function() {
      clearTimeout(timeout);
      timeout = setTimeout(() => func.apply(this, arguments), wait);
    };
  }

  async injectButton() {
    document.querySelectorAll('.ai-reply-button, .tone-selector').forEach(el => el.remove());

    const toolbar = DOMUtils.findFirstElement(CONFIG.SELECTORS.COMPOSE_TOOLBAR);
    if (!toolbar) return;

    const button = AIComponents.createAIButton();
    const toneSelector = AIComponents.createToneSelector();

    toolbar.insertBefore(toneSelector, toolbar.firstChild);
    toolbar.insertBefore(button, toolbar.firstChild);

    button.addEventListener('click', () => this.handleButtonClick(button, toneSelector));
  }

  async handleButtonClick(button, toneSelector) {
    try {
      this.updateButtonState(button, 'loading');

      const emailContent = this.getEmailContent();
      const selectedTone = toneSelector.value;

      const response = await this.generateAIResponse(emailContent, selectedTone);
      this.insertResponseIntoComposeBox(response);

      this.updateButtonState(button, 'success');
    } catch (error) {
      console.error('Erro ao gerar resposta:', error);
      this.updateButtonState(button, 'error');
      this.showErrorNotification(error.message || 'Falha ao gerar resposta');
    }
  }

  updateButtonState(button, state) {
    switch (state) {
      case 'loading':
        button.textContent = CONFIG.BUTTON_TEXT.LOADING;
        button.disabled = true;
        break;
      case 'error':
        button.textContent = CONFIG.BUTTON_TEXT.ERROR;
        button.disabled = false;
        break;
      case 'success':
        default:
          button.textContent = CONFIG.BUTTON_TEXT.DEFAULT;
          button.disabled = false;
    }
  }

  getEmailContent() {
    const content = DOMUtils.findFirstElement(CONFIG.SELECTORS.EMAIL_CONTENT);
    return content ? content.innerText.trim() : '';
  }

  async generateAIResponse(emailContent, tone) {
    try {
      const response = await fetch(CONFIG.API_ENDPOINT, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'X-Extension-Version': chrome.runtime.getManifest().version
        },
        body: JSON.stringify({
          emailContent: emailContent,
          tone: tone
        })
      });

      if (!response.ok) {
        throw new Error(`Erro na API: ${response.status} ${response.statusText}`);
      }

      return await response.text();
    } catch (error) {
      console.error('Erro na requisição:', error);
      throw new Error('Não foi possível conectar ao servidor');
    }
  }

  insertResponseIntoComposeBox(text) {
    const composeBox = document.querySelector(CONFIG.SELECTORS.COMPOSE_BOX);
    if (composeBox) {
      composeBox.focus();
      document.execCommand('insertText', false, text);
    } else {
      throw new Error('Caixa de composição não encontrada');
    }
  }

  showErrorNotification(message) {
    const notification = DOMUtils.createElement('div', {
      'class': 'instantmail-notification',
      'role': 'alert'
    }, {
      position: 'fixed',
      bottom: '20px',
      right: '20px',
      backgroundColor: '#f44336',
      color: 'white',
      padding: '15px',
      borderRadius: '4px',
      zIndex: '9999'
    });

    notification.textContent = message;
    document.body.appendChild(notification);

    setTimeout(() => notification.remove(), 5000);
  }
}

new InstantMailExtension();