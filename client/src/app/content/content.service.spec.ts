/* tslint:disable:no-unused-variable */

import {
  HttpClientTestingModule,
  HttpTestingController
} from "@angular/common/http/testing";
import { HttpClient, HttpClientModule } from "@angular/common/http";
import { Title } from "@angular/platform-browser";
import { TestBed, inject, async } from "@angular/core/testing";
import { ContentService } from "./content.service";
import { Logger } from "../logger/logger.service";
import {
  Location,
  LocationStrategy,
  PathLocationStrategy,
  APP_BASE_HREF
} from "@angular/common";

const mockResponse = {
  content: {
    heading: "Testing"
  }
};

describe("Service: Content", () => {
  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientModule, HttpClientTestingModule],
      providers: [
        { provide: Title, useClass: Title },
        { provide: LocationStrategy, useClass: PathLocationStrategy },
        { provide: APP_BASE_HREF, useValue: "/" },
        Location,
        ContentService,
        Logger
      ]
    });
  });

  it("should create the component", inject(
    [ContentService],
    (service: ContentService) => {
      expect(service).toBeTruthy();
    }
  ));

  it("should return a page",  async(inject(
    [ContentService, HttpClient, HttpTestingController],
    (service: ContentService, http: HttpClient, backend: HttpTestingController) => {
      service.getPage().subscribe(page => {
        expect(page).toBeDefined();
        expect(page.data).toBeDefined();
        //expect(page.data.heading).toEqual("Testing");
      });
      service.load();
      backend.expectOne("http://localhost:9090/kontentsu/api/files/pages/page1/").flush(mockResponse);
    }
  )));

  it("should set title", inject(
    [ContentService, Title, HttpClient, HttpTestingController],
    (service: ContentService, title: Title, http: HttpClient, backend: HttpTestingController) => {
      service.getPage().subscribe(page => {
        //expect(title.getTitle()).toEqual("Test title");
      });
      service.load();
      backend.expectOne("http://localhost:9090/kontentsu/api/files/pages/page1/").flush(mockResponse);
    }
  ));
});
