import { Injectable } from "@angular/core";
import { HttpClient, HttpHeaders } from "@angular/common/http";
import { environment } from "../../environments/environment";
import { Subject, Observable } from "rxjs";
import { Page } from "../page";
import { Logger } from "../logger/logger.service";
import { Title } from "@angular/platform-browser";

@Injectable()
export class ContentService {
  private page: Page;
  private subject: Subject<Page> = new Subject<Page>();

  private headers = new HttpHeaders({
    "Content-Type": "application/json"
  });

  constructor(
    private http: HttpClient,
    private log: Logger,
    private titleService: Title
  ) {}

  load(path?: string): void {
    if (!path) {
      path = environment.frontPage;
    }
    const pageUrl = environment.filesApi + path;
    this.http.get<any>(pageUrl, { headers: this.headers }).subscribe(
      p => {
        if (p.content && p.content.heading) {
          this.log.info(
            'Setting heading from content "' + p.content.heading + '"'
          );
          this.titleService.setTitle(p.content.heading);
        }
        if (p.template && p.template.href) {
          this.log.info("Setting template from content " + p.template.href);
          this.page = new Page(p.template.href, p);
        } else {
          this.log.info(
            "Using default template " + environment.defaultTemplate
          );
          this.page = new Page(environment.defaultTemplate, p);
        }
        this.subject.next(this.page);
      },
      e => {
        this.subject.error("Error getting page at " + pageUrl);
      }
    );
  }

  getPage(): Observable<Page> {
    return this.subject.asObservable();
  }
}
