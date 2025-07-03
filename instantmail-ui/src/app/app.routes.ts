import { Routes } from '@angular/router';
import { EmailGeneratorComponent } from './email-generator/email-generator.component';
import { FeaturesComponent } from './features/features.component';
import { HeroComponent } from './hero/hero.component';

export const routes: Routes = [
  {
    path: 'features', component: FeaturesComponent
  },
  {
    path: 'about', component: HeroComponent
  },
  {
    path: 'emailGenerator', component: EmailGeneratorComponent
  }
];
