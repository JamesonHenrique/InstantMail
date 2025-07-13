# InstantMail

**InstantMail** é uma ferramenta  que utiliza inteligência artificial para ajudar você a redigir respostas de e-mail de forma rápida e eficiente. Com suporte a múltiplos tons de comunicação, o InstantMail se adapta ao seu estilo de escrita, seja profissional, casual ou amigável.

🌐 **Acesse agora:** [https://www.instantmail.shop/](https://www.instantmail.shop/)

## 🚀 Recursos

- Geração automática de respostas de e-mail
- Suporte a diferentes tons de comunicação (profissional, amigável, etc.)
- Interface web responsiva
- Fácil de usar e configurar
- Integração com Gmail através de extensão para Chrome (em desenvolvimento)

## 🛠️ Tecnologias

- **Backend**: Java , Spring Boot
- **Frontend**: Angular (versão compatível com Angular 17+)
- **Ferramentas de Build**: Maven
- **Integração com IA**: Gemini API

## 🌐 Usando o InstantMail

Você pode usar o InstantMail de duas maneiras:

1. **Versão Online (Recomendado)**:
   - Acesse [https://www.instantmail.shop/](https://www.instantmail.shop/)
   - Comece a usar imediatamente sem necessidade de instalação

2. **Versão Local**:
   Siga as instruções abaixo para executar o projeto localmente

## 📦 Pré-requisitos (Apenas para execução local)

- Java 17 ou superior
- Node.js 18+ e npm 9+
- Maven 3.9+
- Conta na Google Cloud com acesso à API do Gemini

## 🚀 Como executar localmente

### 1. Clonar o repositório

```bash
git clone https://github.com/JamesonHenrique/InstantMail.git
cd InstantMail
```

### 2. Configurar as variáveis de ambiente

Crie um arquivo `.env` na raiz do projeto com as seguintes variáveis:

```env
GEMINI_API_KEY=sua_chave_da_api_gemini
GEMINI_API_URL=https://generativelanguage.googleapis.com/v1beta/models/gemini-pro:generateContent?key=
```

### 3. Iniciar o backend

```bash
# Navegue até a pasta do projeto
cd InstantMail

# Compilar e executar o projeto Spring Boot
./mvnw spring-boot:run
```

O servidor estará disponível em `http://localhost:8080`

### 4. Iniciar o frontend

```bash
# Navegue até a pasta do frontend
cd instantmail-ui

# Instalar as dependências
npm install

# Iniciar o servidor de desenvolvimento
ng serve
```

Acesse a aplicação em `http://localhost:4200`

## 🤝 Como contribuir

Contribuições são bem-vindas! Siga estes passos:

1. Faça um Fork do projeto
2. Crie uma Branch para sua Feature (`git checkout -b feature/AmazingFeature`)
3. Adicione suas alterações (`git add .`)
4. Comite suas alterações (`git commit -m 'Adiciona alguma feature incrível'`)
5. Faça o Push da Branch (`git push origin feature/AmazingFeature`)
6. Abra um Pull Request

## 📄 Licença

Este projeto está sob a licença MIT. Veja o arquivo [LICENSE](LICENSE) para mais detalhes.

## ✉️ Contato


Linkedin: [https://www.linkedin.com/in/jamesonhenrique/](https://www.linkedin.com/in/jamesonhenrique/)

Link do Projeto: [https://github.com/JamesonHenrique/InstantMail](https://github.com/JamesonHenrique/InstantMail)

