import { Component } from '@angular/core';
import { HeaderComponent } from './header/header.component';
import { FooterComponent } from './footer/footer.component';
import { FeaturesComponent } from './features/features.component';
import { HeroComponent } from "./hero/hero.component";
import { EmailGeneratorComponent } from "./email-generator/email-generator.component";

@Component({
  selector: 'app-root',
  imports: [HeaderComponent, FooterComponent, FeaturesComponent, HeroComponent, EmailGeneratorComponent],
  templateUrl: './app.component.html',
  styleUrl: './app.component.css',

})
export class AppComponent {
  title = 'InstantMail';
}
