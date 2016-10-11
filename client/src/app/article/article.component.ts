import { Component, Input } from '@angular/core';
import { ContentService } from '../content.service';
@Component({
  selector: 'ko-article',
  templateUrl: './article.component.html',
  styleUrls: ['./article.component.css']
})
export class ArticleComponent {
  @Input() data = {};

  constructor() { }


}
