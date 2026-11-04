import { AddressFields } from '../../shared/address-fields';
import { accountControls } from '../../shared/account-controls';
import { Component, ChangeDetectorRef, inject } from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { RouterModule, Router } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterModule, AddressFields],
  templateUrl: './register.component.html',
  styleUrls: ['./register.component.scss'],
})
export class RegisterComponent {
  registerForm: FormGroup;
  isLoading = false;
  errorMessage = '';
  successMessage = '';
  private cdr = inject(ChangeDetectorRef);
  isAdmin() {
    return this.authService.hasRole('ADMIN');
  }

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private router: Router,
  ) {
    this.registerForm = this.fb.group({
      name: ['', Validators.required],
      email: ['', [Validators.required, Validators.email]],
      password: ['', [Validators.required, Validators.minLength(8), Validators.maxLength(72)]],
      role: ['USER', Validators.required],
      ...accountControls(),
      confirmPassword: ['', Validators.required],
    });
  }

  hasFieldError(fieldName: string, errorName: string): boolean {
    const field = this.registerForm.get(fieldName);
    return !!field && field.hasError(errorName) && (field.dirty || field.touched);
  }

  onSubmit(): void {
    this.errorMessage = '';
    if (this.registerForm.value.password !== this.registerForm.value.confirmPassword) {
      this.errorMessage = 'As senhas devem ser iguais.';
      return;
    }
    if (this.registerForm.invalid) {
      this.errorMessage = 'Confira os campos obrigatórios de contato e endereço.';
      this.registerForm.markAllAsTouched();
      return;
    }

    this.isLoading = true;
    this.errorMessage = '';
    this.successMessage = '';
    this.authService.register(this.registerForm.value).subscribe({
      next: () => {
        this.isLoading = false;
        this.successMessage = 'Conta criada com sucesso.';
        if (this.isAdmin()) void this.router.navigateByUrl('/admin/users');
        else void this.router.navigateByUrl('/login');
        this.cdr.detectChanges();
      },
      error: () => {
        this.isLoading = false;
        this.errorMessage =
          'Não foi possível criar a conta. Confira se o e-mail já está cadastrado.';
        this.cdr.detectChanges();
      },
    });
  }
}
