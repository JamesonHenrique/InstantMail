import { CommonModule } from '@angular/common';
import { Component, ElementRef, ViewChild } from '@angular/core';
import {
  FormBuilder,
  FormGroup,
  FormsModule,
  ReactiveFormsModule,
  Validators,
} from '@angular/forms';
import { catchError, of } from 'rxjs';
import { EmailGeneratorService } from '../services/email-generator.service';

interface EmailForm {
  originalEmail: string;
  tone: string;
}

@Component({
  selector: 'app-email-generator',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, FormsModule],
  templateUrl: './email-generator.component.html',
  styleUrls: ['./email-generator.component.css'],
})
export class EmailGeneratorComponent {
  @ViewChild('resultArea') resultArea!: ElementRef;
  @ViewChild('submitButton') submitButton!: ElementRef;

  emailForm: FormGroup;
  isLoading = false;
  showResult = false;
  generatedReply = '';
  isCopied = false;
  error = '';
  toneOptions = [
    { value: 'professional', label: 'Profissional' },
    { value: 'friendly', label: 'Amigável' },
    { value: 'casual', label: 'Casual' },
    { value: 'urgent', label: 'Urgente' },
  ];
  errorMessage: string = '';

  constructor(private fb: FormBuilder, private emailGeneratorService: EmailGeneratorService) {
    this.emailForm = this.fb.group({
      originalEmail: ['', [Validators.required, Validators.minLength(5)]],
      tone: [''],
    });
  }

  async copyToClipboard(): Promise<void> {
    try {
      await navigator.clipboard.writeText(this.generatedReply);
      this.isCopied = true;
      setTimeout(() => {
        this.isCopied = false;
      }, 2000);
    } catch (err) {
      console.error('Falha ao copiar texto: ', err);
    }
  }

  handleSubmit() {
    if (this.emailForm.invalid) {
      return;
    }

    this.isLoading = true;


    const formData = this.emailForm.value;

    this.emailGeneratorService.generateEmail(formData.originalEmail, formData.tone)
      .pipe(
        catchError((error) => {
          this.errorMessage = 'Falha ao gerar resposta. Tente novamente.';
          console.error(error);
          return of(null);
        })
      )
      .subscribe((response) => {
        if (response) {
          this.generatedReply = response;
        }
        this.showResult = true;
        this.isLoading = false;
      });
  }
}


