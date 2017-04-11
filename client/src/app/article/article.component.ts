import { Component, Input } from '@angular/core';
import { ContentService } from '../content.service';
@Component({
  selector: 'k-article',
  templateUrl: './article.component.html',
  styleUrls: ['./article.component.css']
})
export class ArticleComponent {
  @Input() data = {};

  constructor() { }

}
