import { Component, EventEmitter, Input, Output, inject } from '@angular/core';
import { brazilianPhone, whatsappLink } from '../core/contact';
import { AuthService } from '../core/services/auth.service';
import { ServiceCardData, Summary, money } from '../core/market.models';
@Component({
  selector: 'app-service-card',
  standalone: true,
  template: ` <article class="market-card">
    <div class="card-heading">
      <div>
        <span class="eyebrow">{{ category() }}</span>
        <h2>{{ service.name }}</h2>
      </div>
      @if (service.distance !== undefined) {
        <span class="badge">{{ service.distance.toFixed(1) }} km</span>
      }
    </div>
    <p class="hint">Sobre o serviço</p>
    <p class="service-description">
      {{ service.description || 'Entre em contato para conhecer os serviços oferecidos.' }}
    </p>
    <p class="hint">{{ service.city }} · {{ service.state }}</p>
    <p>
      <strong>{{ price(service.startingPrice) }}</strong
      ><span class="hint">
        ·
        {{
          service.acceptingRequests === false ? 'Agenda indisponível' : 'Recebendo solicitações'
        }}</span
      >
    </p>
    <button type="button" class="text-button" (click)="reviews.emit()">
      {{
        summary?.count
          ? summary!.average.toFixed(1) + ' / 5 · ' + summary!.count + ' avaliação(ões)'
          : 'Ainda sem avaliações'
      }}
    </button>
    @if (contactNumber()) {
      <p class="hint">WhatsApp do fornecedor: {{ contactNumber() }}</p>
    } @else {
      <p class="hint">WhatsApp não informado pelo fornecedor.</p>
    }
    <div class="actions">
      @if (whatsapp(); as url) {
        <a
          class="btn-primary"
          [href]="url"
          target="_blank"
          rel="noopener noreferrer"
          [attr.aria-label]="'Conversar no WhatsApp sobre ' + service.name"
          >Conversar no WhatsApp</a
        >
      }
      @if (auth.hasRole('USER')) {
        <button
          type="button"
          class="btn-secondary"
          [disabled]="service.acceptingRequests === false"
          (click)="request.emit()"
        >
          Solicitar orçamento
        </button>
        <button
          type="button"
          class="btn-secondary"
          [disabled]="busy"
          [attr.aria-pressed]="favorite"
          (click)="toggle.emit()"
        >
          {{ favorite ? 'Remover favorito' : 'Salvar favorito' }}
        </button>
      }
    </div>
  </article>`,
})
export class ServiceCard {
  @Input({ required: true }) service!: ServiceCardData;
  @Input() summary: Summary | undefined;
  @Input() favorite = false;
  @Input() busy = false;
  @Output() request = new EventEmitter<void>();
  @Output() toggle = new EventEmitter<void>();
  @Output() reviews = new EventEmitter<void>();
  auth = inject(AuthService);
  price = money;
  category() {
    return typeof this.service.category === 'string'
      ? this.service.category
      : (this.service.category?.name ?? 'Serviços gerais');
  }
  contactNumber() {
    const phone = brazilianPhone(this.service.phone);
    if (!phone) return '';
    const local = phone.slice(2);
    return '(' + local.slice(0, 2) + ') ' + local.slice(2, -4) + '-' + local.slice(-4);
  }
  whatsapp() {
    return whatsappLink(this.service.phone, this.service.name);
  }
}
