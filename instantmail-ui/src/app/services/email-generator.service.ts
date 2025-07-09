import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../environment/environment.dev';

@Injectable({
  providedIn: 'root'
})
export class EmailGeneratorService {

  constructor(private http: HttpClient) { }
  generateEmail(emailContent: string, tone: string): Observable<string> {
    return this.http.post(environment.apiUrl + '/api/email/generate', {
      emailContent,
      tone,
    }, { responseType: 'text' })
  }
}
