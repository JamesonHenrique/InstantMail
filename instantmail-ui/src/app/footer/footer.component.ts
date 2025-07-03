import { Component } from '@angular/core';

@Component({
  selector: 'app-footer',
  imports: [],
  templateUrl: './footer.component.html',
  styleUrl: './footer.component.css'
})
export class FooterComponent {
  scrollTo(sectionId: string) {
    const element = document.getElementById(sectionId);
    if (element) {
      element.scrollIntoView({ behavior: 'smooth' });
    }
  }
  links: string[] = [
    "https://www.linkedin.com/in/jamesonhenrique/",
    "https://github.com/JamesonHenrique",
    "mailto:jamesonhenrique14@gmail.com"
  ]
  goToLink(link: string) {
    window.open(link, '_blank')
  }

}
