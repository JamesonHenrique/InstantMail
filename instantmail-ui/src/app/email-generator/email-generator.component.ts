import { CommonModule } from '@angular/common';
import { Component, ElementRef, ViewChild } from '@angular/core';
import {
  FormBuilder,
  FormGroup,
  FormsModule,
  ReactiveFormsModule,
} from '@angular/forms';
import { catchError, of } from 'rxjs';
import { HttpClient } from '@angular/common/http';

interface EmailForm {
  originalEmail: string;
  tone: string;
  includeGreeting: boolean;
  includeSignature: boolean;
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
    { value: 'professional', label: 'Professional' },
    { value: 'friendly', label: 'Friendly' },
    { value: 'formal', label: 'Formal' },
    { value: 'casual', label: 'Casual' },
    { value: 'urgent', label: 'Urgent' },
  ];

  constructor(private fb: FormBuilder, private http: HttpClient) {
    this.emailForm = this.fb.group({
      originalEmail: [''],
      tone: [''],
      includeGreeting: [false],
      includeSignature: [false],
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
      console.error('Failed to copy text: ', err);
    }
  }

  handleSubmit() {
    if (this.emailForm.invalid) {
      return;
    }

    this.isLoading = true;
    this.error = '';

    const formData = this.emailForm.value;

    this.http
      .post(
        'http://localhost:8080/api/email/generate',
        {
          emailContent: formData.originalEmail,
          tone: formData.tone,
          includeGreeting: formData.includeGreeting,
          includeSignature: formData.includeSignature,
        },
        { responseType: 'text' }
      )
      .pipe(
        catchError((error) => {
          this.error = 'Failed to generate email reply. Please try again.';
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
