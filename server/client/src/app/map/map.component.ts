import { Component, OnInit } from '@angular/core';
import { HttpClient, HttpParams, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs/Observable';
import 'rxjs/add/operator/map';

@Component({
  selector: 'app-map',
  templateUrl: './map.component.html',
  styleUrls: ['./map.component.css']
})
export class MapComponent implements OnInit {
  constructor(private http: HttpClient) {}

  readonly ROOT_URL = '/api/getLatestData';

  posts: Observable<any>;

  ngOnInit() {
    this.posts = this.http.get(this.ROOT_URL);
  }
}
