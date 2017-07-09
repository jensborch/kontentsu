import { Router } from '@angular/router';
import { HostListener, Directive, Input } from '@angular/core';

@Directive({
  selector: '[href]'
})
export class LinkDirective {

  @Input() href;
  @HostListener('click', ['$event']) onClick(event) { this.routeLink(event); }

  constructor(private router: Router) { }

  private routeLink(event) {
    if (this.href.length > 0 && !this.href.startsWith('href')) {
      event.preventDefault();
      this.router.navigateByUrl(this.href);
    }
  }

}
