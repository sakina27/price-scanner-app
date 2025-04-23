from fastapi import FastAPI, HTTPException
from selenium import webdriver
from selenium.webdriver.common.by import By
from selenium.webdriver.chrome.service import Service
from selenium.webdriver.chrome.options import Options
from selenium.webdriver.support.ui import WebDriverWait
from selenium.webdriver.support import expected_conditions as EC
from selenium.common.exceptions import StaleElementReferenceException, TimeoutException, NoSuchElementException
from webdriver_manager.chrome import ChromeDriverManager
import asyncio
import time

app = FastAPI()

# Function to setup Selenium WebDriver
def setup_driver():
    chrome_options = Options()
    chrome_options.add_argument("--headless")  # Run in headless mode
    chrome_options.add_argument("--disable-gpu")
    chrome_options.add_argument("--no-sandbox")
    chrome_options.add_argument("--disable-dev-shm-usage")
    chrome_options.add_argument("--window-size=1920,1080")
    chrome_options.add_argument("--disable-blink-features=AutomationControlled")
    chrome_options.add_argument("user-agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/119.0.0.0 Safari/537.36")  # Set User-Agent


    service = Service(ChromeDriverManager().install())
    driver = webdriver.Chrome(service=service, options=chrome_options)
    return driver

# Function to extract numeric value from price strings
def extract_price(price_text):
    try:
        return float(price_text.replace("₹", "").replace(",", "").strip())
    except ValueError:
        return None  # Handle cases where price extraction fails

def scrape_jiomart(query: str):
    driver = setup_driver()
    search_url = f"https://www.jiomart.com/search?q={query}"
    driver.get(search_url)

    try:
        WebDriverWait(driver, 10).until(EC.presence_of_element_located((By.CLASS_NAME, "plp-card-wrapper")))
    except:
        driver.quit()
        return {"error": "No products found or page structure changed"}
    
    products = driver.find_elements(By.CLASS_NAME, "plp-card-wrapper")
    product_data = []
    
    for product in products:
        try:
            #name_element = product.find_element(By.CLASS_NAME, "plp-card-title")
            #name = name_element.text.strip() if name_element else "No Name"
            name = product.get_attribute("title")
            #price_elements = product.find_elements(By.CLASS_NAME, "plp-card-details-price")
            #price = price_elements[0].text.strip() if price_elements else "Price Not Available"

            price_elements = product.find_elements(By.CLASS_NAME, "plp-card-details-price")
            price = "Price Not Available"

            if price_elements:
                try:
                    price_span = price_elements[0].find_element(By.TAG_NAME, "span")  # Get the first span inside
                    price = price_span.text.strip()
                except:
                    pass 
            
            price = extract_price(price.split("\n")[0]) if price else None
            
            discount_elements = product.find_elements(By.CLASS_NAME, "plp-card-details-discount")
            discount = discount_elements[0].text.strip() if discount_elements else "No Discount"
            
            image_element = product.find_element(By.CLASS_NAME, "plp-card-image").find_element(By.TAG_NAME, "img")
            image_url = image_element.get_attribute("src") if image_element else "No Image"
            
            #product_url_element = product.find_element(By.TAG_NAME, "a")
            #product_url = product_url_element.get_attribute("href") if product_url_element else "No URL"
            product_url = product.get_attribute("href")
            
            product_data.append({
                "Name": name,
                "Price": price,
                "Original Price": discount,
                "Image URL": image_url,
                "Product URL": product_url,
                "Source": "Jio-Mart"
            })
        except Exception as e:
            print(f"Error extracting product: {e}")
    
    driver.quit()
    return product_data

# Scrape Zepto data (unchanged)
def scrape_zepto(query: str):
    driver = setup_driver()
    search_url = f"https://www.zepto.com/search?query={query}"
    driver.get(search_url)

    wait = WebDriverWait(driver, 10)
    product_data = []

    try:
        while True:
            try:
                products = wait.until(EC.presence_of_all_elements_located((By.CSS_SELECTOR, "h5[data-testid='product-card-name']")))
                prices = wait.until(EC.presence_of_all_elements_located((By.CSS_SELECTOR, "h4[data-testid='product-card-price']")))
                discount_prices = driver.find_elements(By.CSS_SELECTOR, "p.line-through")
                images = wait.until(EC.presence_of_all_elements_located((By.CSS_SELECTOR, "img[data-testid='product-card-image']")))
                links = wait.until(EC.presence_of_all_elements_located((By.CSS_SELECTOR, "a[data-testid='product-card']")))
                
                for i in range(len(products)):
                    try:
                        name = products[i].text.strip()
                        price = extract_price(prices[i].text.strip())
                        discount = extract_price(discount_prices[i].text.strip()) if i < len(discount_prices) else price
                        image_url = images[i].get_attribute("src")
                        product_url = links[i].get_attribute("href")

                        product_data.append({
                            "Name": name,
                            "Price": price,
                            "Original Price": discount,
                            "Image URL": image_url,
                            "Product URL": product_url,
                            "Source": "Zepto"
                        })
                    except StaleElementReferenceException:
                        continue
                break  
            except StaleElementReferenceException:
                continue  
    except TimeoutException:
        raise HTTPException(status_code=500, detail="Failed to fetch data from Zepto")
    finally:
        driver.quit()

    return product_data

def scrape_swiggy(query: str):
    driver = setup_driver()
    search_url = f"https://www.swiggy.com/instamart/search?query={query}"
    driver.get(search_url)
    time.sleep(5)  # Wait for page to load
    
    products = driver.find_elements(By.CSS_SELECTOR, "div[data-testid='default_container_ux4']")
    
    scraped_data = []
    for product in products:
        try:
            name = product.find_element(By.CLASS_NAME, "novMV").text
            image = product.find_element(By.TAG_NAME, "img").get_attribute("src")
            price = product.find_element(By.CSS_SELECTOR, "div[data-testid='itemMRPPrice'] div").text
            quantity = product.find_element(By.CLASS_NAME, "_3eIPt").text
            product_url = search_url  # Extract individual product URL
            price = extract_price(price.split("\n")[0]) if price else None
            scraped_data.append({
                "Name": name,
                "Price": price,
                "Original Price": quantity,
                "Image URL": image,
                "Product URL": product_url,
                "Source": "Swiggy Instamart"
            })
        except Exception as e:
            print(f"Error extracting data for a product: {e}")
    
    driver.quit()
    return scraped_data

def scrape_bigbasket(query: str):
    driver = setup_driver()
    search_url = f"https://www.bigbasket.com/ps/?q={query}"
    driver.get(search_url)

    try:
        WebDriverWait(driver, 10).until(
            EC.presence_of_element_located((By.CSS_SELECTOR, 'div.SKUDeck___StyledDiv-sc-1e5d9gk-0'))
        )
    except:
        print("No products found.")
        driver.quit()
        return []

    # Scroll down multiple times to load more products
    for _ in range(5):
        driver.execute_script("window.scrollBy(0, 1000);")
        time.sleep(1)
    
    products = driver.find_elements(By.CSS_SELECTOR, 'div.SKUDeck___StyledDiv-sc-1e5d9gk-0')
    product_data = []

    for product in products:
        try:
            name = product.find_element(By.CSS_SELECTOR, 'h3 a').text.replace("\n", " ").strip()
        except:
            name = None
        
        try:
            image = product.find_element(By.CSS_SELECTOR, 'img').get_attribute('src')
        except:
            image = None
        
        try:
            price = product.find_element(By.CSS_SELECTOR, 'span[class*="Pricing"]').text.strip()
        except:
            price = None
        
        try:
            quantity = product.find_element(By.CSS_SELECTOR, 'button[class*="PackChanger"]').text.strip()
        except:
            quantity = None
        
        try:
            product_url = product.find_element(By.CSS_SELECTOR, 'h3 a').get_attribute('href')
        except:
            product_url = None
        price = extract_price(price.split("\n")[0]) if price else None
        product_data.append({
            "Name": name,
            "Price": price,
            "Original Price": quantity,
            "Image URL": image,
            "Product URL": product_url,
            "Source": "Big Basket"
        })
    
    driver.quit()
    return product_data

def scrape_starquik(query: str):
    driver = setup_driver()
    search_url = f"https://www.starquik.com/search/{query.replace(' ', '%20')}"
    driver.get(search_url)

    wait = WebDriverWait(driver, 10)
    product_data = []

    try:
        while True:
            try:
                products = wait.until(EC.presence_of_all_elements_located((By.CLASS_NAME, "product-card-container")))
                
                for product in products:
                    try:
                        name = product.find_element(By.CLASS_NAME, "product-card-name-container").text.strip()
                        price = product.find_element(By.CLASS_NAME, "product-card-price-container-price").text.strip()
                        quantity = product.find_element(By.CLASS_NAME, "two-line-ellipsis").text.strip()
                        img_url = product.find_element(By.CLASS_NAME, "product-image-container").get_attribute("src")
                        product_url = product.find_element(By.TAG_NAME, "a").get_attribute("href")
                        price = extract_price(price.split("\n")[0]) if price else None
                        product_data.append({
                            "Name": name,
                            "Price": price,
                            "Quantity": quantity,
                            "Image URL": img_url,
                            "Product URL": product_url,
                            "Source": "StarQuik"
                        })
                    except StaleElementReferenceException:
                        continue  # Skip stale elements
                break  
            except StaleElementReferenceException:
                continue  # Retry if elements are not stable
    except TimeoutException:
        print("Failed to fetch data from StarQuik")
    finally:
        driver.quit()

    return product_data

# Combine results from both Zepto and JioMart
@app.get("/search")
async def search_products(query: str):
    loop = asyncio.get_event_loop()
    
    # Run both scraping functions concurrently
    zepto_task = loop.run_in_executor(None, scrape_zepto, query)
    jiomart_task = loop.run_in_executor(None, scrape_jiomart, query)
    swiggy_task = loop.run_in_executor(None, scrape_swiggy, query)
    bigbasket_task = loop.run_in_executor(None, scrape_bigbasket, query)
    starquik_task = loop.run_in_executor(None, scrape_starquik, query)
    zepto_results, jiomart_results, swiggy_results, bigbasket_results, starquik_results = await asyncio.gather(zepto_task, jiomart_task, swiggy_task, bigbasket_task, starquik_task)

    # Merge and sort results by price
    combined_results = zepto_results + jiomart_results + swiggy_results + bigbasket_results + starquik_results
    for item in combined_results:
       print(f"Keys in item: {item.keys()}")


    combined_results.sort(key=lambda x: float(x["Price"]) if x["Price"] not in [None, "", "N/A"] else float("inf"))


    return combined_results