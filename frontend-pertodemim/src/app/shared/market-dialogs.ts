import {
  AfterViewInit,
  Component,
  ElementRef,
  EventEmitter,
  Input,
  Output,
  ViewChild,
  inject,
  signal,
} from '@angular/core';
import { FormsModule } from '@angular/forms';
import { DatePipe } from '@angular/common';
import { finalize } from 'rxjs';
import { ApiService } from '../core/services/api.service';
import { Page } from '../core/models';
import { ServiceCardData, ServiceReview } from '../core/market.models';

@Component({
  selector: 'app-request-dialog',
  standalone: true,
  imports: [FormsModule],
  template: ` <dialog #dialog aria-labelledby="request-title" (cancel)="cancelEvent($event)">
    <h2 id="request-title">Solicitar orçamento</h2>
    <p class="hint">{{ service.name }} · O preço final será combinado com o profissional.</p>
    <form (ngSubmit)="submit()" novalidate>
      <div class="field">
        <label for="request-description">O que você precisa?</label
        ><textarea
          id="request-description"
          name="description"
          [(ngModel)]="description"
          rows="5"
          maxlength="2000"
          placeholder="Descreva o serviço e os detalhes necessários"
        ></textarea>
      </div>
      <div class="field">
        <label for="request-date">Data desejada (opcional)</label
        ><input id="request-date" name="date" type="date" [min]="today" [(ngModel)]="date" />
      </div>
      <p class="hint">
        Seu nome e telefone cadastrado serão compartilhados com o profissional para atender esta
        solicitação.
      </p>
      @if (error()) {
        <p class="feedback error" role="alert">{{ error() }}</p>
      }
      <div class="actions">
        <button class="btn-secondary" type="button" [disabled]="busy()" (click)="cancel.emit()">
          Cancelar</button
        ><button class="btn-primary" [disabled]="busy()">
          {{ busy() ? 'Enviando...' : 'Enviar solicitação' }}
        </button>
      </div>
    </form>
  </dialog>`,
})
export class RequestDialog implements AfterViewInit {
  @Input({ required: true }) service!: ServiceCardData;
  @Output() cancel = new EventEmitter<void>();
  @Output() done = new EventEmitter<void>();
  @ViewChild('dialog') dialog!: ElementRef<HTMLDialogElement>;
  private api = inject(ApiService);
  busy = signal(false);
  error = signal('');
  description = '';
  date = '';
  today = new Date().toLocaleDateString('sv-SE');
  private key = crypto.randomUUID();
  ngAfterViewInit() {
    this.dialog.nativeElement.showModal();
  }
  cancelEvent(event: Event) {
    event.preventDefault();
    if (!this.busy()) this.cancel.emit();
  }
  submit() {
    this.error.set('');
    if (this.busy()) return;
    if (
      this.description.trim().length < 10 ||
      this.description.length > 2000 ||
      (this.date && this.date < this.today)
    ) {
      this.error.set('Descreva sua necessidade em pelo menos 10 caracteres e confira a data.');
      return;
    }
    this.busy.set(true);
    this.api
      .post('market/requests', {
        serviceId: this.service.id,
        description: this.description.trim(),
        preferredDate: this.date || null,
        requestKey: this.key,
      })
      .pipe(finalize(() => this.busy.set(false)))
      .subscribe({
        next: () => this.done.emit(),
        error: () =>
          this.error.set(
            'Não foi possível enviar. Confira sua conexão e a disponibilidade do serviço.',
          ),
      });
  }
}

@Component({
  selector: 'app-reviews-dialog',
  standalone: true,
  imports: [DatePipe],
  template: ` <dialog #dialog aria-labelledby="reviews-title" (cancel)="cancel.emit()">
    <h2 id="reviews-title">Avaliações de {{ name }}</h2>
    <p class="hint">Avaliações de atendimentos concluídos e confirmados.</p>
    @if (loading()) {
      <p role="status">Carregando...</p>
    }
    @if (error()) {
      <p class="feedback error" role="alert">{{ error() }}</p>
    }
    @for (review of reviews(); track review.id) {
      <article class="review-item">
        <strong>{{ review.rating }} / 5 · {{ review.customerName }}</strong>
        <p>{{ review.comment }}</p>
        <small class="hint">{{ review.createdAt | date: 'dd/MM/yyyy' }}</small>
      </article>
    }
    @if (!loading() && !error() && !reviews().length) {
      <p class="state-box">Este serviço ainda não tem avaliações.</p>
    }
    <div class="pagination">
      <button
        class="btn-secondary"
        [disabled]="loading() || page() === 0"
        (click)="load(page() - 1)"
      >
        Anterior</button
      ><button
        class="btn-secondary"
        [disabled]="loading() || page() + 1 >= pages()"
        (click)="load(page() + 1)"
      >
        Próxima
      </button>
    </div>
    <button class="btn-primary" (click)="cancel.emit()">Fechar avaliações</button>
  </dialog>`,
})
export class ReviewsDialog implements AfterViewInit {
  @Input({ required: true }) serviceId!: number;
  @Input() name = '';
  @Output() cancel = new EventEmitter<void>();
  @ViewChild('dialog') dialog!: ElementRef<HTMLDialogElement>;
  private api = inject(ApiService);
  reviews = signal<ServiceReview[]>([]);
  loading = signal(false);
  error = signal('');
  page = signal(0);
  pages = signal(0);
  ngAfterViewInit() {
    this.dialog.nativeElement.showModal();
    this.load();
  }
  load(page = 0) {
    this.loading.set(true);
    this.error.set('');
    this.api
      .get<Page<ServiceReview>>(`market/services/${this.serviceId}/reviews?page=${page}`)
      .pipe(finalize(() => this.loading.set(false)))
      .subscribe({
        next: (data) => {
          this.reviews.set(data.content);
          this.page.set(data.number);
          this.pages.set(data.totalPages);
        },
        error: () => this.error.set('Não foi possível carregar as avaliações.'),
      });
  }
}

@Component({
  selector: 'app-review-dialog',
  standalone: true,
  imports: [FormsModule],
  template: ` <dialog #dialog aria-labelledby="review-title" (cancel)="cancelEvent($event)">
    <h2 id="review-title">Avaliar atendimento</h2>
    <p>{{ name }}</p>
    <form (ngSubmit)="submit()" novalidate>
      <div class="field">
        <label for="review-rating">Nota</label
        ><select id="review-rating" name="rating" [(ngModel)]="rating">
          <option [ngValue]="5">5 · Excelente</option>
          <option [ngValue]="4">4 · Muito bom</option>
          <option [ngValue]="3">3 · Bom</option>
          <option [ngValue]="2">2 · Regular</option>
          <option [ngValue]="1">1 · Ruim</option>
        </select>
      </div>
      <div class="field">
        <label for="review-comment">Como foi sua experiência?</label
        ><textarea
          id="review-comment"
          name="comment"
          [(ngModel)]="comment"
          rows="4"
          maxlength="1000"
        ></textarea>
      </div>
      <p class="hint">
        Seu nome, nota e comentário ficarão visíveis para outros usuários. Evite dados pessoais no
        comentário.
      </p>
      @if (error()) {
        <p class="feedback error" role="alert">{{ error() }}</p>
      }
      <div class="actions">
        <button class="btn-secondary" type="button" [disabled]="busy()" (click)="cancel.emit()">
          Cancelar</button
        ><button class="btn-primary" [disabled]="busy()">
          {{ busy() ? 'Enviando...' : 'Publicar avaliação' }}
        </button>
      </div>
    </form>
  </dialog>`,
})
export class ReviewDialog implements AfterViewInit {
  @Input({ required: true }) requestId!: number;
  @Input() name = '';
  @Output() cancel = new EventEmitter<void>();
  @Output() done = new EventEmitter<void>();
  @ViewChild('dialog') dialog!: ElementRef<HTMLDialogElement>;
  private api = inject(ApiService);
  rating = 5;
  comment = '';
  busy = signal(false);
  error = signal('');
  ngAfterViewInit() {
    this.dialog.nativeElement.showModal();
  }
  cancelEvent(event: Event) {
    event.preventDefault();
    if (!this.busy()) this.cancel.emit();
  }
  submit() {
    this.error.set('');
    if (this.busy()) return;
    if (this.comment.trim().length < 10 || this.rating < 1 || this.rating > 5) {
      this.error.set('Informe nota de 1 a 5 e um comentário com pelo menos 10 caracteres.');
      return;
    }
    this.busy.set(true);
    this.api
      .post(`market/requests/${this.requestId}/review`, {
        rating: this.rating,
        comment: this.comment.trim(),
      })
      .pipe(finalize(() => this.busy.set(false)))
      .subscribe({
        next: () => this.done.emit(),
        error: () =>
          this.error.set('Não foi possível publicar. Confira se este atendimento já foi avaliado.'),
      });
  }
}
