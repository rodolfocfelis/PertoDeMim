/** Normaliza telefone brasileiro com DDD; não presume que existe conta no WhatsApp. */
export function brazilianPhone(value: string | null | undefined): string | null {
  let digits = (value ?? '').replace(/\D/g, '');
  if ((digits.length === 12 || digits.length === 13) && digits.startsWith('55')) {
    digits = digits.slice(2);
  }
  if (!/^[1-9][0-9](?:[2-5][0-9]{7}|9[0-9]{8})$/.test(digits)) return null;
  return '55' + digits;
}
export function whatsappLink(phone: string | null | undefined, serviceName: string): string | null {
  const number = brazilianPhone(phone);
  if (!number) return null;
  const message =
    'Olá! Encontrei o serviço "' + serviceName + '" no PertoDeMim e gostaria de mais informações.';
  return 'https://wa.me/' + number + '?text=' + encodeURIComponent(message);
}
