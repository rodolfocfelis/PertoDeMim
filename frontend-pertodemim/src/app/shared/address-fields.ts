import { Component, Input, inject, signal } from '@angular/core';
import { FormGroup, ReactiveFormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { timeout, finalize } from 'rxjs';
@Component({
  selector: 'app-address-fields',
  standalone: true,
  imports: [ReactiveFormsModule],
  template: ` <p class="hint">Telefone, CEP, rua, número, cidade e estado são obrigatórios.</p>
    <div [formGroup]="group" class="form-grid">
      <div class="field">
        <label [for]="prefix + 'phone'">Telefone com DDD</label
        ><input
          [id]="prefix + 'phone'"
          formControlName="phone"
          autocomplete="tel"
          placeholder="11999999999"
        />
      </div>
      <div class="field">
        <label [for]="prefix + 'zip'">CEP</label
        ><input
          [id]="prefix + 'zip'"
          formControlName="zipCode"
          autocomplete="postal-code"
          maxlength="9"
          (blur)="lookup()"
        />
      </div>
      <div class="field">
        <label [for]="prefix + 'street'">Rua / avenida</label
        ><input [id]="prefix + 'street'" formControlName="street" autocomplete="address-line1" />
      </div>
      <div class="field">
        <label [for]="prefix + 'number'">Número</label
        ><input
          [id]="prefix + 'number'"
          formControlName="addressNumber"
          placeholder="Número ou S/N"
          maxlength="20"
        />
      </div>
      <div class="field">
        <label [for]="prefix + 'district'">Bairro</label
        ><input [id]="prefix + 'district'" formControlName="neighborhood" />
      </div>
      <div class="field">
        <label [for]="prefix + 'complement'">Complemento (opcional)</label
        ><input
          [id]="prefix + 'complement'"
          formControlName="complement"
          autocomplete="address-line2"
        />
      </div>
      <div class="field">
        <label [for]="prefix + 'city'">Cidade</label
        ><input [id]="prefix + 'city'" formControlName="city" autocomplete="address-level2" />
      </div>
      <div class="field">
        <label [for]="prefix + 'state'">Estado (UF)</label
        ><input
          [id]="prefix + 'state'"
          formControlName="state"
          maxlength="2"
          autocomplete="address-level1"
        />
      </div>
    </div>
    @if (busy()) {
      <p class="hint" role="status">Consultando CEP...</p>
    }
    @if (message()) {
      <p class="hint" role="status">{{ message() }}</p>
    }
    <p class="hint">
      O endereço cadastrado será usado na busca quando o GPS estiver indisponível.
    </p>`,
})
export class AddressFields {
  @Input({ required: true }) group!: FormGroup;
  @Input() prefix = 'address-';
  private http = inject(HttpClient);
  busy = signal(false);
  message = signal('');
  lookup() {
    const cep = String(this.group.get('zipCode')?.value ?? '').replace(/\D/g, '');
    if (cep.length !== 8) return;
    this.busy.set(true);
    this.message.set('');
    this.http
      .get<any>(`https://viacep.com.br/ws/${cep}/json/`)
      .pipe(
        timeout(8000),
        finalize(() => this.busy.set(false)),
      )
      .subscribe({
        next: (data) => {
          if (data.erro) {
            this.message.set('CEP não encontrado. Confira o endereço.');
            return;
          }
          this.group.patchValue({
            street: data.logradouro,
            neighborhood: data.bairro,
            city: data.localidade,
            state: data.uf,
          });
        },
        error: () =>
          this.message.set('Consulta de CEP indisponível. Preencha o endereço manualmente.'),
      });
  }
}
